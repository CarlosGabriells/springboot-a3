#!/bin/bash

# Library Management System - Data Validation Script
# This script validates the populated data and checks business rules

BASE_URL="http://localhost:8080/api"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TOTAL_VALIDATIONS=0
PASSED_VALIDATIONS=0
FAILED_VALIDATIONS=0

# Function to print colored output
print_validation() {
    echo -e "${BLUE}[VALIDATION]${NC} $1"
}

print_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED_VALIDATIONS++))
}

print_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED_VALIDATIONS++))
}

print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

# Function to run a validation
run_validation() {
    local validation_name="$1"
    local expected_result="$2"
    local actual_result="$3"
    
    ((TOTAL_VALIDATIONS++))
    print_validation "$validation_name"
    
    if [ "$expected_result" = "$actual_result" ]; then
        print_pass "Expected: $expected_result, Got: $actual_result"
    else
        print_fail "Expected: $expected_result, Got: $actual_result"
    fi
    echo
}

# Function to get JSON value
get_json_value() {
    echo "$1" | jq -r "$2" 2>/dev/null || echo "null"
}

# Function to get array length
get_json_length() {
    echo "$1" | jq -r '. | length' 2>/dev/null || echo "0"
}

# Function to check if API is running
check_api() {
    print_info "Checking if API is running..."
    if curl -s -f "$BASE_URL/authors" > /dev/null 2>&1; then
        print_info "API is running!"
        return 0
    else
        print_fail "API is not running or not accessible at $BASE_URL"
        return 1
    fi
}

# Validate data counts
validate_data_counts() {
    echo "=== DATA COUNT VALIDATIONS ==="
    
    # Check authors count
    authors_response=$(curl -s "$BASE_URL/authors?size=20")
    authors_content=$(echo "$authors_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    authors_count=$(get_json_length "$authors_content")
    run_validation "Authors count" "14" "$authors_count"
    
    # Check categories count
    categories_response=$(curl -s "$BASE_URL/categories?size=20")
    categories_content=$(echo "$categories_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    categories_count=$(get_json_length "$categories_content")
    run_validation "Categories count" "8" "$categories_count"
    
    # Check books count
    books_response=$(curl -s "$BASE_URL/books?size=20")
    books_content=$(echo "$books_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    books_count=$(get_json_length "$books_content")
    run_validation "Books count" "12" "$books_count"
    
    # Check members count
    members_response=$(curl -s "$BASE_URL/members?size=20")
    members_content=$(echo "$members_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    members_count=$(get_json_length "$members_content")
    run_validation "Members count" "10" "$members_count"
    
    # Check loans count
    loans_response=$(curl -s "$BASE_URL/loans?size=20")
    loans_content=$(echo "$loans_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    loans_count=$(get_json_length "$loans_content")
    run_validation "Loans count" "9" "$loans_count"
}

# Validate business rules
validate_business_rules() {
    echo "=== BUSINESS RULES VALIDATIONS ==="
    
    # Check active members
    active_members_response=$(curl -s "$BASE_URL/members/status/ACTIVE?size=20")
    active_members_content=$(echo "$active_members_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    active_members_count=$(get_json_length "$active_members_content")
    run_validation "Active members count" "9" "$active_members_count"
    
    # Check available books
    available_books_response=$(curl -s "$BASE_URL/books/available?size=20")
    available_books_content=$(echo "$available_books_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    available_books_count=$(get_json_length "$available_books_content")
    
    if [ "$available_books_count" -gt 0 ]; then
        print_pass "Available books exist ($available_books_count books)"
        ((PASSED_VALIDATIONS++))
    else
        print_fail "No available books found"
        ((FAILED_VALIDATIONS++))
    fi
    ((TOTAL_VALIDATIONS++))
    echo
    
    # Check active loans
    active_loans_response=$(curl -s "$BASE_URL/loans/status/ACTIVE?size=20")
    active_loans_content=$(echo "$active_loans_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    active_loans_count=$(get_json_length "$active_loans_content")
    
    if [ "$active_loans_count" -gt 0 ]; then
        print_pass "Active loans exist ($active_loans_count loans)"
        ((PASSED_VALIDATIONS++))
    else
        print_fail "No active loans found"
        ((FAILED_VALIDATIONS++))
    fi
    ((TOTAL_VALIDATIONS++))
    echo
    
    # Check overdue loans
    overdue_loans_response=$(curl -s "$BASE_URL/loans/status/OVERDUE?size=20")
    overdue_loans_content=$(echo "$overdue_loans_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    overdue_loans_count=$(get_json_length "$overdue_loans_content")
    
    if [ "$overdue_loans_count" -gt 0 ]; then
        print_pass "Overdue loans detected ($overdue_loans_count loans)"
        ((PASSED_VALIDATIONS++))
    else
        print_info "No overdue loans found (this is good!)"
        ((PASSED_VALIDATIONS++))
    fi
    ((TOTAL_VALIDATIONS++))
    echo
}

# Validate data integrity
validate_data_integrity() {
    echo "=== DATA INTEGRITY VALIDATIONS ==="
    
    # Check that all books have authors
    books_response=$(curl -s "$BASE_URL/books?size=20")
    books_content=$(echo "$books_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    books_with_authors=0
    
    if [ -n "$books_content" ] && [ "$books_content" != "null" ] && [ "$books_content" != "[]" ]; then
        books_with_authors=$(echo "$books_content" | jq -r '[.[] | select(.author != null and .author.firstName != null and .author.lastName != null)] | length' 2>/dev/null || echo "0")
    fi
    
    books_count=$(get_json_length "$books_content")
    run_validation "Books with authors" "$books_count" "$books_with_authors"
    
    # Check that all loans have valid members and books
    loans_response=$(curl -s "$BASE_URL/loans?size=20")
    loans_content=$(echo "$loans_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    valid_loans=0
    
    if [ -n "$loans_content" ] && [ "$loans_content" != "null" ] && [ "$loans_content" != "[]" ]; then
        valid_loans=$(echo "$loans_content" | jq -r '[.[] | select(.member != null and .book != null and .member.email != null and .book.isbn != null)] | length' 2>/dev/null || echo "0")
    fi
    
    loans_count=$(get_json_length "$loans_content")
    run_validation "Loans with valid references" "$loans_count" "$valid_loans"
    
    # Check unique ISBNs
    unique_isbns=$(echo "$books_content" | jq -r '[.[].isbn] | unique | length' 2>/dev/null || echo "0")
    total_books=$(get_json_length "$books_content")
    run_validation "Unique ISBNs" "$total_books" "$unique_isbns"
    
    # Check unique member emails
    members_response=$(curl -s "$BASE_URL/members?size=20")
    members_content=$(echo "$members_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    unique_emails=$(echo "$members_content" | jq -r '[.[].email] | unique | length' 2>/dev/null || echo "0")
    total_members=$(get_json_length "$members_content")
    run_validation "Unique member emails" "$total_members" "$unique_emails"
}

# Validate specific sample data
validate_sample_data() {
    echo "=== SAMPLE DATA VALIDATIONS ==="
    
    # Check for specific authors
    authors_response=$(curl -s "$BASE_URL/authors?size=20")
    authors_content=$(echo "$authors_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    
    # Check if J.K. Rowling exists (using firstName and lastName)
    jk_rowling_exists=$(echo "$authors_content" | jq -r '[.[] | select(.firstName == "Joanne" and .lastName == "Rowling")] | length' 2>/dev/null || echo "0")
    run_validation "J.K. Rowling exists" "1" "$jk_rowling_exists"
    
    # Check if George Orwell exists
    george_orwell_exists=$(echo "$authors_content" | jq -r '[.[] | select(.firstName == "George" and .lastName == "Orwell")] | length' 2>/dev/null || echo "0")
    run_validation "George Orwell exists" "1" "$george_orwell_exists"
    
    # Check for specific books
    books_response=$(curl -s "$BASE_URL/books?size=20")
    books_content=$(echo "$books_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    
    # Check if Harry Potter exists
    harry_potter_exists=$(echo "$books_content" | jq -r '[.[] | select(.title | contains("Harry Potter"))] | length' 2>/dev/null || echo "0")
    
    if [ "$harry_potter_exists" -gt 0 ]; then
        print_pass "Harry Potter books exist ($harry_potter_exists books)"
        ((PASSED_VALIDATIONS++))
    else
        print_fail "No Harry Potter books found"
        ((FAILED_VALIDATIONS++))
    fi
    ((TOTAL_VALIDATIONS++))
    echo
    
    # Check if 1984 exists
    book_1984_exists=$(echo "$books_content" | jq -r '[.[] | select(.title == "1984")] | length' 2>/dev/null || echo "0")
    run_validation "Book '1984' exists" "1" "$book_1984_exists"
    
    # Check for Fantasy category
    categories_response=$(curl -s "$BASE_URL/categories?size=20")
    categories_content=$(echo "$categories_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    fantasy_exists=$(echo "$categories_content" | jq -r '[.[] | select(.name == "Fantasy")] | length' 2>/dev/null || echo "0")
    run_validation "Fantasy category exists" "1" "$fantasy_exists"
}

# Check database health
check_database_health() {
    echo "=== DATABASE HEALTH CHECK ==="
    
    # Test response times
    print_info "Checking API response times..."
    
    start_time=$(date +%s.%N)
    curl -s "$BASE_URL/authors" > /dev/null
    end_time=$(date +%s.%N)
    authors_time=$(echo "$end_time - $start_time" | bc 2>/dev/null || echo "0")
    
    print_info "Authors endpoint response time: ${authors_time}s"
    
    # Check if response time is reasonable (less than 2 seconds)
    if (( $(echo "$authors_time < 2.0" | bc -l 2>/dev/null || echo "1") )); then
        print_pass "Authors endpoint response time is acceptable"
        ((PASSED_VALIDATIONS++))
    else
        print_fail "Authors endpoint response time is too slow"
        ((FAILED_VALIDATIONS++))
    fi
    ((TOTAL_VALIDATIONS++))
    echo
    
    # Test all endpoints are accessible
    endpoints=("/authors" "/categories" "/books" "/members" "/loans")
    accessible_endpoints=0
    
    for endpoint in "${endpoints[@]}"; do
        if curl -s -f "$BASE_URL$endpoint" > /dev/null 2>&1; then
            ((accessible_endpoints++))
        fi
    done
    
    run_validation "Accessible endpoints" "5" "$accessible_endpoints"
}

# Main execution
main() {
    print_info "Starting Data Validation..."
    print_info "API Base URL: $BASE_URL"
    echo
    
    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        print_fail "jq is required but not installed. Please install jq first."
        exit 1
    fi
    
    # Check if bc is installed (for time calculations)
    if ! command -v bc &> /dev/null; then
        print_info "bc not found - some time calculations will be skipped"
    fi
    
    # Check API availability
    if ! check_api; then
        exit 1
    fi
    
    echo
    
    # Run all validation suites
    validate_data_counts
    validate_business_rules
    validate_data_integrity
    validate_sample_data
    check_database_health
    
    # Print summary
    echo "=== VALIDATION SUMMARY ==="
    print_info "Total Validations: $TOTAL_VALIDATIONS"
    print_pass "Passed: $PASSED_VALIDATIONS"
    print_fail "Failed: $FAILED_VALIDATIONS"
    
    if [ $FAILED_VALIDATIONS -eq 0 ]; then
        print_pass "All validations passed! 🎉"
        print_info "Your Library Management System is working correctly!"
        exit 0
    else
        print_fail "Some validations failed. Please check the output above."
        exit 1
    fi
}

# Run the script
main "$@"