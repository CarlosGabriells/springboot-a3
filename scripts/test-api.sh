#!/bin/bash

# Library Management System - API Testing Script
# This script tests all REST API endpoints with comprehensive scenarios

BASE_URL="http://localhost:8080/api"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print colored output
print_test() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

print_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED_TESTS++))
}

print_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED_TESTS++))
}

print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

# Function to run a test
run_test() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_code="$5"
    
    ((TOTAL_TESTS++))
    print_test "$test_name"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            "$BASE_URL$endpoint")
    fi
    
    http_code="${response: -3}"
    response_body="${response%???}"
    
    if [ "$http_code" -eq "$expected_code" ]; then
        print_pass "HTTP $http_code (Expected: $expected_code)"
        if [ -n "$response_body" ] && [ "$response_body" != "null" ]; then
            echo "Response: $(echo "$response_body" | jq -C . 2>/dev/null || echo "$response_body")"
        fi
    else
        print_fail "HTTP $http_code (Expected: $expected_code)"
        if [ -n "$response_body" ]; then
            echo "Response: $response_body"
        fi
    fi
    echo
}

# Function to check if API is running
check_api() {
    print_info "Checking if API is running..."
    if curl -s -f "$BASE_URL/authors" > /dev/null 2>&1; then
        print_info "API is running!"
        return 0
    else
        print_fail "API is not running or not accessible at $BASE_URL"
        print_fail "Please start the application with: mvn spring-boot:run"
        return 1
    fi
}

# Test Authors endpoints
test_authors() {
    echo "=== AUTHOR ENDPOINTS ==="
    
    # Test GET all authors
    run_test "Get all authors" "GET" "/authors" "" 200
    
    # Test POST create author
    author_data='{"name":"Test Author","biography":"Test biography","birthDate":"1990-01-01","nationality":"Test Country"}'
    run_test "Create new author" "POST" "/authors" "$author_data" 201
    
    # Test GET single author (assuming ID 1 exists)
    run_test "Get author by ID" "GET" "/authors/1" "" 200
    
    # Test GET non-existent author
    run_test "Get non-existent author" "GET" "/authors/999" "" 404
    
    # Test PUT update author
    update_data='{"name":"Updated Author","biography":"Updated biography","birthDate":"1990-01-01","nationality":"Updated Country"}'
    run_test "Update author" "PUT" "/authors/1" "$update_data" 200
    
    # Test invalid data
    invalid_data='{"name":"","biography":"Test"}'
    run_test "Create author with invalid data" "POST" "/authors" "$invalid_data" 400
}

# Test Categories endpoints
test_categories() {
    echo "=== CATEGORY ENDPOINTS ==="
    
    # Test GET all categories
    run_test "Get all categories" "GET" "/categories" "" 200
    
    # Test POST create category
    category_data='{"name":"Test Category","description":"Test category description"}'
    run_test "Create new category" "POST" "/categories" "$category_data" 201
    
    # Test GET single category
    run_test "Get category by ID" "GET" "/categories/1" "" 200
    
    # Test duplicate category name
    run_test "Create duplicate category" "POST" "/categories" "$category_data" 400
}

# Test Books endpoints
test_books() {
    echo "=== BOOK ENDPOINTS ==="
    
    # Test GET all books
    run_test "Get all books" "GET" "/books" "" 200
    
    # Test GET available books
    run_test "Get available books" "GET" "/books/available" "" 200
    
    # Test GET books by author
    run_test "Get books by author" "GET" "/books/author/1" "" 200
    
    # Test GET books by category
    run_test "Get books by category" "GET" "/books/category/1" "" 200
    
    # Test POST create book
    book_data='{"title":"Test Book","isbn":"9999999999999","publicationDate":"2023-01-01","totalCopies":5,"availableCopies":5,"authorName":"J.K. Rowling","categories":["Fantasy"]}'
    run_test "Create new book" "POST" "/books" "$book_data" 201
    
    # Test search books
    run_test "Search books by title" "GET" "/books/search?title=Harry" "" 200
}

# Test Members endpoints
test_members() {
    echo "=== MEMBER ENDPOINTS ==="
    
    # Test GET all members
    run_test "Get all members" "GET" "/members" "" 200
    
    # Test GET active members
    run_test "Get active members" "GET" "/members/active" "" 200
    
    # Test POST create member
    member_data='{"name":"Test Member","email":"test@example.com","phone":"+1-555-9999","address":"Test Address","membershipDate":"2023-01-01","active":true}'
    run_test "Create new member" "POST" "/members" "$member_data" 201
    
    # Test GET single member
    run_test "Get member by ID" "GET" "/members/1" "" 200
    
    # Test duplicate email
    run_test "Create member with duplicate email" "POST" "/members" "$member_data" 400
}

# Test Loans endpoints
test_loans() {
    echo "=== LOAN ENDPOINTS ==="
    
    # Test GET all loans
    run_test "Get all loans" "GET" "/loans" "" 200
    
    # Test GET active loans
    run_test "Get active loans" "GET" "/loans/active" "" 200
    
    # Test GET overdue loans
    run_test "Get overdue loans" "GET" "/loans/overdue" "" 200
    
    # Test GET loans by member
    run_test "Get loans by member" "GET" "/loans/member/1" "" 200
    
    # Test POST create loan
    loan_data='{"memberEmail":"john.smith@email.com","bookIsbn":"9780747532699","loanDate":"2024-12-01","dueDate":"2024-12-15","status":"ACTIVE"}'
    run_test "Create new loan" "POST" "/loans" "$loan_data" 201
}

# Test error scenarios
test_error_scenarios() {
    echo "=== ERROR SCENARIOS ==="
    
    # Test malformed JSON
    run_test "Malformed JSON request" "POST" "/authors" '{"name":"Test"' 400
    
    # Test unsupported HTTP method
    run_test "Unsupported method" "PATCH" "/authors/1" "" 405
    
    # Test non-existent endpoint
    run_test "Non-existent endpoint" "GET" "/nonexistent" "" 404
}

# Performance tests
test_performance() {
    echo "=== PERFORMANCE TESTS ==="
    
    print_info "Running performance test - 10 concurrent requests to /authors"
    
    start_time=$(date +%s.%N)
    
    for i in {1..10}; do
        curl -s "$BASE_URL/authors" > /dev/null &
    done
    wait
    
    end_time=$(date +%s.%N)
    duration=$(echo "$end_time - $start_time" | bc)
    
    print_info "10 concurrent requests completed in ${duration}s"
    echo
}

# Main execution
main() {
    print_info "Starting API Testing..."
    print_info "API Base URL: $BASE_URL"
    echo
    
    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        print_info "jq not found - JSON responses will not be pretty-printed"
    fi
    
    # Check API availability
    if ! check_api; then
        exit 1
    fi
    
    echo
    
    # Run all test suites
    test_authors
    test_categories
    test_books
    test_members
    test_loans
    test_error_scenarios
    test_performance
    
    # Print summary
    echo "=== TEST SUMMARY ==="
    print_info "Total Tests: $TOTAL_TESTS"
    print_pass "Passed: $PASSED_TESTS"
    print_fail "Failed: $FAILED_TESTS"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        print_pass "All tests passed! 🎉"
        exit 0
    else
        print_fail "Some tests failed. Please check the output above."
        exit 1
    fi
}

# Run the script
main "$@"