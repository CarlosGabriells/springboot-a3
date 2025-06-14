# Sample Data for Library Management System

This folder contains sample data that can be used to populate your Library Management System for testing and demonstration purposes.

## Files

- **authors.json** - 8 famous authors with biographical information
- **categories.json** - 8 book categories (Fantasy, Science Fiction, Mystery, etc.)
- **books.json** - 12 books linked to authors and categories
- **members.json** - 10 library members with contact information
- **loans.json** - 10 loan records showing active, returned, and overdue loans

## Data Relationships

The sample data maintains proper relationships:
- Books are linked to authors by author name
- Books have multiple categories
- Loans reference members by email and books by ISBN
- Loan statuses include: ACTIVE, RETURNED, OVERDUE

## Usage

You can use this data to:
1. Test your REST API endpoints
2. Populate a development database
3. Demonstrate the system functionality
4. Create integration tests

## Data Volume

- 8 Authors
- 8 Categories  
- 12 Books (52 total copies, 24 available)
- 10 Members (9 active, 1 inactive)
- 10 Loans (6 active, 3 returned, 1 overdue)