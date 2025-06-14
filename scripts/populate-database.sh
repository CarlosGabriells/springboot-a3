#!/bin/bash

# Library Management System - Database Population Script
# This script populates the database with sample data via REST API calls

BASE_URL="http://localhost:8080/api"
SAMPLE_DATA_DIR="./sample-data"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if the API is running
check_api() {
    print_status "Checking if API is running..."
    if curl -s -f "$BASE_URL/authors" > /dev/null 2>&1; then
        print_status "API is running!"
        return 0
    else
        print_error "API is not running or not accessible at $BASE_URL"
        print_error "Please start the application with: mvn spring-boot:run"
        return 1
    fi
}

# Function to populate authors
populate_authors() {
    print_status "Populating authors..."
    
    while IFS= read -r line; do
        author=$(echo "$line" | jq -c '.')
        name=$(echo "$author" | jq -r '.firstName + " " + .lastName')
        
        response=$(curl -s -w "%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$author" \
            "$BASE_URL/authors")
        
        http_code="${response: -3}"
        if [ "$http_code" -eq 201 ] || [ "$http_code" -eq 200 ]; then
            print_status "✓ Created author: $name"
        else
            print_warning "⚠ Failed to create author: $name (HTTP $http_code)"
        fi
    done < <(jq -c '.[]' "$SAMPLE_DATA_DIR/authors.json")
}

# Function to populate categories
populate_categories() {
    print_status "Populating categories..."
    
    while IFS= read -r line; do
        category=$(echo "$line" | jq -c '.')
        name=$(echo "$category" | jq -r '.name')
        
        response=$(curl -s -w "%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$category" \
            "$BASE_URL/categories")
        
        http_code="${response: -3}"
        if [ "$http_code" -eq 201 ] || [ "$http_code" -eq 200 ]; then
            print_status "✓ Created category: $name"
        else
            print_warning "⚠ Failed to create category: $name (HTTP $http_code)"
        fi
    done < <(jq -c '.[]' "$SAMPLE_DATA_DIR/categories.json")
}

# Function to get author ID by name
get_author_id_by_name() {
    local first_name="$1"
    local last_name="$2"
    
    authors_response=$(curl -s "$BASE_URL/authors")
    content=$(echo "$authors_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    
    echo "$content" | jq -r --arg fname "$first_name" --arg lname "$last_name" \
        '.[] | select(.firstName == $fname and .lastName == $lname) | .id' | head -1
}

# Function to get category ID by name
get_category_id_by_name() {
    local category_name="$1"
    
    categories_response=$(curl -s "$BASE_URL/categories")
    content=$(echo "$categories_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    
    echo "$content" | jq -r --arg name "$category_name" \
        '.[] | select(.name == $name) | .id' | head -1
}

# Function to populate books with dynamic ID resolution
populate_books() {
    print_status "Populating books..."
    
    # First get all authors and categories for ID mapping
    authors_response=$(curl -s "$BASE_URL/authors")
    categories_response=$(curl -s "$BASE_URL/categories")
    
    while IFS= read -r line; do
        book=$(echo "$line" | jq -c '.')
        title=$(echo "$book" | jq -r '.title')
        
        # Get the expected author ID based on position (1-based index to match original data)
        original_author_id=$(echo "$book" | jq -r '.authorId')
        
        # Map original author ID to actual author by getting the nth author
        authors_content=$(echo "$authors_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
        actual_author_id=$(echo "$authors_content" | jq -r ".[$((original_author_id-1))].id // empty")
        
        if [ -z "$actual_author_id" ] || [ "$actual_author_id" = "null" ]; then
            print_warning "⚠ Failed to find author for book: $title (original ID: $original_author_id)"
            continue
        fi
        
        # Map category IDs similarly
        original_category_ids=$(echo "$book" | jq -r '.categoryIds[]?' 2>/dev/null || echo "")
        actual_category_ids="[]"
        
        if [ -n "$original_category_ids" ]; then
            categories_content=$(echo "$categories_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
            actual_category_ids="["
            first=true
            for orig_cat_id in $original_category_ids; do
                actual_cat_id=$(echo "$categories_content" | jq -r ".[$((orig_cat_id-1))].id // empty")
                if [ -n "$actual_cat_id" ] && [ "$actual_cat_id" != "null" ]; then
                    if [ "$first" = true ]; then
                        actual_category_ids="${actual_category_ids}${actual_cat_id}"
                        first=false
                    else
                        actual_category_ids="${actual_category_ids},${actual_cat_id}"
                    fi
                fi
            done
            actual_category_ids="${actual_category_ids}]"
        fi
        
        # Create updated book JSON with correct IDs
        updated_book=$(echo "$book" | jq \
            --arg author_id "$actual_author_id" \
            --argjson category_ids "$actual_category_ids" \
            '.authorId = ($author_id | tonumber) | .categoryIds = $category_ids')
        
        response=$(curl -s -w "%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$updated_book" \
            "$BASE_URL/books")
        
        http_code="${response: -3}"
        if [ "$http_code" -eq 201 ] || [ "$http_code" -eq 200 ]; then
            print_status "✓ Created book: $title (Author ID: $actual_author_id)"
        else
            response_body="${response%???}"
            print_warning "⚠ Failed to create book: $title (HTTP $http_code)"
            if [ -n "$response_body" ]; then
                echo "   Response: $response_body"
            fi
        fi
    done < <(jq -c '.[]' "$SAMPLE_DATA_DIR/books.json")
}

# Function to populate members
populate_members() {
    print_status "Populating members..."
    
    while IFS= read -r line; do
        member=$(echo "$line" | jq -c '.')
        name=$(echo "$member" | jq -r '.firstName + " " + .lastName')
        
        response=$(curl -s -w "%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$member" \
            "$BASE_URL/members")
        
        http_code="${response: -3}"
        if [ "$http_code" -eq 201 ] || [ "$http_code" -eq 200 ]; then
            print_status "✓ Created member: $name"
        else
            print_warning "⚠ Failed to create member: $name (HTTP $http_code)"
        fi
    done < <(jq -c '.[]' "$SAMPLE_DATA_DIR/members.json")
}

# Function to populate loans with dynamic ID resolution
populate_loans() {
    print_status "Populating loans..."
    
    # Get current members and books for ID mapping
    members_response=$(curl -s "$BASE_URL/members")
    books_response=$(curl -s "$BASE_URL/books")
    
    while IFS= read -r line; do
        loan=$(echo "$line" | jq -c '.')
        original_member_id=$(echo "$loan" | jq -r '.memberId')
        original_book_id=$(echo "$loan" | jq -r '.bookId')
        
        # Map original member ID to actual member by getting the nth member
        members_content=$(echo "$members_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
        actual_member_id=$(echo "$members_content" | jq -r ".[$((original_member_id-1))].id // empty")
        
        # Map original book ID to actual book by getting the nth book
        books_content=$(echo "$books_response" | jq -r '.content // .' 2>/dev/null || echo "[]")
        actual_book_id=$(echo "$books_content" | jq -r ".[$((original_book_id-1))].id // empty")
        
        if [ -z "$actual_member_id" ] || [ "$actual_member_id" = "null" ]; then
            print_warning "⚠ Failed to find member for loan (original ID: $original_member_id)"
            continue
        fi
        
        if [ -z "$actual_book_id" ] || [ "$actual_book_id" = "null" ]; then
            print_warning "⚠ Failed to find book for loan (original ID: $original_book_id)"
            continue
        fi
        
        # Create updated loan JSON with correct IDs
        updated_loan=$(echo "$loan" | jq \
            --arg member_id "$actual_member_id" \
            --arg book_id "$actual_book_id" \
            '.memberId = ($member_id | tonumber) | .bookId = ($book_id | tonumber)')
        
        response=$(curl -s -w "%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$updated_loan" \
            "$BASE_URL/loans")
        
        http_code="${response: -3}"
        if [ "$http_code" -eq 201 ] || [ "$http_code" -eq 200 ]; then
            print_status "✓ Created loan: Member $actual_member_id -> Book $actual_book_id"
        else
            response_body="${response%???}"
            print_warning "⚠ Failed to create loan: Member $actual_member_id -> Book $actual_book_id (HTTP $http_code)"
            if [ -n "$response_body" ]; then
                echo "   Response: $response_body"
            fi
        fi
    done < <(jq -c '.[]' "$SAMPLE_DATA_DIR/loans.json")
}

# Main execution
main() {
    print_status "Starting database population..."
    print_status "Sample data directory: $SAMPLE_DATA_DIR"
    print_status "API Base URL: $BASE_URL"
    echo
    
    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        print_error "jq is required but not installed. Please install jq first."
        print_error "On macOS: brew install jq"
        print_error "On Ubuntu: sudo apt-get install jq"
        exit 1
    fi
    
    # Check if sample data files exist
    for file in authors.json categories.json books.json members.json loans.json; do
        if [ ! -f "$SAMPLE_DATA_DIR/$file" ]; then
            print_error "Sample data file not found: $SAMPLE_DATA_DIR/$file"
            exit 1
        fi
    done
    
    # Check API availability
    if ! check_api; then
        exit 1
    fi
    
    echo
    print_status "Populating database in order..."
    
    # Populate in correct order (respecting foreign key constraints)
    populate_authors
    echo
    populate_categories
    echo
    populate_books
    echo
    populate_members
    echo
    populate_loans
    
    echo
    print_status "Database population completed!"
    print_status "You can now test the API endpoints or view the data via Swagger UI:"
    print_status "http://localhost:8080/swagger-ui.html"
}

# Run the script
main "$@"