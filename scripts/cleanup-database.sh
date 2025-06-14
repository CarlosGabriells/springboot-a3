#!/bin/bash

# Library Management System - Database Cleanup Script
# This script removes all sample data from the database

BASE_URL="http://localhost:8080/api"

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

# Function to check if API is running
check_api() {
    print_status "Checking if API is running..."
    if curl -s -f "$BASE_URL/authors" > /dev/null 2>&1; then
        print_status "API is running!"
        return 0
    else
        print_error "API is not running or not accessible at $BASE_URL"
        return 1
    fi
}

# Function to get all records and delete them
cleanup_entity() {
    local entity_name="$1"
    local endpoint="$2"
    
    print_status "Cleaning up $entity_name..."
    
    # Get all records
    response=$(curl -s "$BASE_URL$endpoint")
    
    if [ -z "$response" ] || [ "$response" = "[]" ] || [ "$response" = "null" ]; then
        print_status "No $entity_name found to delete"
        return 0
    fi
    
    # Extract content array from paginated response or use response directly
    content=$(echo "$response" | jq -r '.content // .' 2>/dev/null || echo "[]")
    
    if [ "$content" = "[]" ] || [ "$content" = "null" ]; then
        print_status "No $entity_name found to delete"
        return 0
    fi
    
    # Count records
    count=$(echo "$content" | jq -r '. | length' 2>/dev/null || echo "0")
    print_status "Found $count $entity_name records to delete"
    
    # Delete each record
    deleted_count=0
    failed_count=0
    
    if [ "$count" -gt 0 ]; then
        echo "$content" | jq -c '.[]' | while read -r record; do
            id=$(echo "$record" | jq -r '.id' 2>/dev/null)
            
            if [ -n "$id" ] && [ "$id" != "null" ]; then
                delete_response=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL$endpoint/$id")
                http_code="${delete_response: -3}"
                
                if [ "$http_code" -eq 204 ] || [ "$http_code" -eq 200 ]; then
                    echo "✓ Deleted $entity_name ID: $id"
                    ((deleted_count++))
                else
                    echo "⚠ Failed to delete $entity_name ID: $id (HTTP $http_code)"
                    ((failed_count++))
                fi
            fi
        done
    fi
}

# Function to confirm cleanup
confirm_cleanup() {
    echo
    print_warning "⚠️  WARNING: This will delete ALL data from your Library Management System!"
    print_warning "This includes all authors, books, categories, members, and loans."
    echo
    read -p "Are you sure you want to continue? (yes/no): " confirmation
    
    case $confirmation in
        yes|YES|y|Y)
            print_status "Proceeding with cleanup..."
            return 0
            ;;
        *)
            print_status "Cleanup cancelled."
            return 1
            ;;
    esac
}

# Function to force cleanup (skip confirmation)
force_cleanup() {
    print_warning "Force cleanup mode - skipping confirmation"
    return 0
}

# Main cleanup function
perform_cleanup() {
    print_status "Starting database cleanup..."
    echo
    
    # Delete in reverse order to respect foreign key constraints
    # Loans -> Books -> Categories, Authors -> Members
    
    cleanup_entity "loans" "/loans"
    echo
    
    cleanup_entity "books" "/books"
    echo
    
    cleanup_entity "categories" "/categories"
    echo
    
    cleanup_entity "authors" "/authors"
    echo
    
    cleanup_entity "members" "/members"
    echo
    
    print_status "Cleanup completed!"
}

# Function to verify cleanup
verify_cleanup() {
    print_status "Verifying cleanup..."
    echo
    
    endpoints=("/authors" "/categories" "/books" "/members" "/loans")
    entity_names=("authors" "categories" "books" "members" "loans")
    
    for i in "${!endpoints[@]}"; do
        endpoint="${endpoints[$i]}"
        entity_name="${entity_names[$i]}"
        
        response=$(curl -s "$BASE_URL$endpoint")
        content=$(echo "$response" | jq -r '.content // .' 2>/dev/null || echo "[]")
        count=$(echo "$content" | jq -r '. | length' 2>/dev/null || echo "0")
        
        if [ "$count" -eq 0 ]; then
            print_status "✓ $entity_name: 0 records (clean)"
        else
            print_warning "⚠ $entity_name: $count records remaining"
        fi
    done
    
    echo
    print_status "Verification completed!"
}

# Function to show current data counts
show_current_data() {
    print_status "Current data in database:"
    echo
    
    endpoints=("/authors" "/categories" "/books" "/members" "/loans")
    entity_names=("Authors" "Categories" "Books" "Members" "Loans")
    
    for i in "${!endpoints[@]}"; do
        endpoint="${endpoints[$i]}"
        entity_name="${entity_names[$i]}"
        
        response=$(curl -s "$BASE_URL$endpoint")
        content=$(echo "$response" | jq -r '.content // .' 2>/dev/null || echo "[]")
        count=$(echo "$content" | jq -r '. | length' 2>/dev/null || echo "0")
        
        printf "%-12s: %s records\n" "$entity_name" "$count"
    done
    echo
}

# Function to backup data before cleanup
backup_data() {
    local backup_dir="backup-$(date +%Y%m%d-%H%M%S)"
    print_status "Creating backup in $backup_dir..."
    
    mkdir -p "$backup_dir"
    
    endpoints=("/authors" "/categories" "/books" "/members" "/loans")
    filenames=("authors.json" "categories.json" "books.json" "members.json" "loans.json")
    
    for i in "${!endpoints[@]}"; do
        endpoint="${endpoints[$i]}"
        filename="${filenames[$i]}"
        
        curl -s "$BASE_URL$endpoint" | jq '.' > "$backup_dir/$filename" 2>/dev/null
        print_status "✓ Backed up $filename"
    done
    
    print_status "Backup completed in $backup_dir/"
    echo
}

# Show help
show_help() {
    echo "Library Management System - Database Cleanup Script"
    echo
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  -f, --force      Skip confirmation prompt"
    echo "  -b, --backup     Create backup before cleanup"
    echo "  -s, --show       Show current data counts only"
    echo "  -v, --verify     Verify database is clean (check for remaining data)"
    echo "  -h, --help       Show this help message"
    echo
    echo "Examples:"
    echo "  $0                 # Interactive cleanup with confirmation"
    echo "  $0 -f              # Force cleanup without confirmation"
    echo "  $0 -b -f           # Backup data then force cleanup"
    echo "  $0 -s              # Show current data counts"
    echo "  $0 -v              # Verify database is clean"
}

# Main execution
main() {
    local force=false
    local backup=false
    local show_only=false
    local verify_only=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -f|--force)
                force=true
                shift
                ;;
            -b|--backup)
                backup=true
                shift
                ;;
            -s|--show)
                show_only=true
                shift
                ;;
            -v|--verify)
                verify_only=true
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    print_status "Library Management System - Database Cleanup"
    print_status "API Base URL: $BASE_URL"
    echo
    
    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        print_error "jq is required but not installed. Please install jq first."
        exit 1
    fi
    
    # Check API availability
    if ! check_api; then
        exit 1
    fi
    
    echo
    
    # Handle different modes
    if [ "$show_only" = true ]; then
        show_current_data
        exit 0
    fi
    
    if [ "$verify_only" = true ]; then
        verify_cleanup
        exit 0
    fi
    
    # Show current data
    show_current_data
    
    # Confirm cleanup
    if [ "$force" = true ]; then
        force_cleanup
    else
        if ! confirm_cleanup; then
            exit 0
        fi
    fi
    
    # Create backup if requested
    if [ "$backup" = true ]; then
        backup_data
    fi
    
    # Perform cleanup
    perform_cleanup
    
    # Verify cleanup
    verify_cleanup
    
    print_status "Database cleanup completed successfully! 🎉"
}

# Run the script
main "$@"