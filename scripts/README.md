# Library Management System - Testing Scripts

This directory contains comprehensive scripts to help you populate, test, and validate your Library Management System.

## Scripts Overview

### 🗄️ `populate-database.sh`
Populates your database with sample data from the `sample-data/` directory.

**Usage:**
```bash
./scripts/populate-database.sh
```

**Features:**
- Populates data in correct order (respecting foreign keys)
- Colored output with status indicators
- Error handling and validation
- Requires API to be running on http://localhost:8080

### 🧪 `test-api.sh`
Comprehensive API testing suite that tests all endpoints.

**Usage:**
```bash
./scripts/test-api.sh
```

**Test Categories:**
- **Author endpoints** - CRUD operations, validation
- **Category endpoints** - Create, read, duplicate handling
- **Book endpoints** - Search, filtering, availability
- **Member endpoints** - Active members, validation
- **Loan endpoints** - Active, overdue, member loans
- **Error scenarios** - Invalid requests, malformed JSON
- **Performance tests** - Concurrent request handling

### ✅ `validate-data.sh`
Validates populated data and checks business rules.

**Usage:**
```bash
./scripts/validate-data.sh
```

**Validation Categories:**
- **Data counts** - Verify expected number of records
- **Business rules** - Active members, available books, loan limits
- **Data integrity** - Foreign key relationships, unique constraints
- **Sample data** - Specific authors, books, categories exist
- **Database health** - Response times, endpoint accessibility

### 🧹 `cleanup-database.sh`
Removes all data from the database with safety features.

**Usage:**
```bash
# Interactive cleanup (with confirmation)
./scripts/cleanup-database.sh

# Force cleanup (no confirmation)
./scripts/cleanup-database.sh --force

# Create backup before cleanup
./scripts/cleanup-database.sh --backup --force

# Show current data counts
./scripts/cleanup-database.sh --show

# Verify database is clean
./scripts/cleanup-database.sh --verify
```

## Prerequisites

### Required Tools
- **curl** - For API requests
- **jq** - For JSON parsing and formatting
- **bc** - For time calculations (optional)

### Installation
```bash
# macOS
brew install jq

# Ubuntu/Debian
sudo apt-get install jq curl bc

# CentOS/RHEL
sudo yum install jq curl bc
```

### Application Setup
1. Start your Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

2. Verify API is accessible:
   ```bash
   curl http://localhost:8080/api/authors
   ```

## Complete Testing Workflow

### 1. Start Fresh
```bash
# Clean any existing data
./scripts/cleanup-database.sh --force

# Populate with sample data
./scripts/populate-database.sh
```

### 2. Validate Data
```bash
# Verify data was populated correctly
./scripts/validate-data.sh
```

### 3. Test API
```bash
# Run comprehensive API tests
./scripts/test-api.sh
```

### 4. Manual Testing
- Visit Swagger UI: http://localhost:8080/swagger-ui.html
- Test specific endpoints manually
- Verify business logic

### 5. Cleanup (Optional)
```bash
# Remove test data
./scripts/cleanup-database.sh --backup
```

## Script Features

### 🎨 Colored Output
All scripts use colored output for better readability:
- 🟢 **Green** - Success/Pass
- 🟡 **Yellow** - Warning/Info
- 🔴 **Red** - Error/Fail
- 🔵 **Blue** - Test/Validation

### 📊 Progress Tracking
- Real-time status updates
- Success/failure counters
- Detailed error reporting
- Summary statistics

### 🛡️ Safety Features
- API availability checks
- Dependency validation
- Confirmation prompts
- Backup capabilities
- Error handling

### ⚡ Performance
- Parallel API calls where possible
- Response time monitoring
- Concurrent request testing
- Efficient JSON processing

## Troubleshooting

### Common Issues

**API Not Running**
```bash
# Error: API is not running or not accessible
# Solution: Start the application
mvn spring-boot:run
```

**Missing Dependencies**
```bash
# Error: jq command not found
# Solution: Install jq
brew install jq  # macOS
sudo apt-get install jq  # Ubuntu
```

**Permission Denied**
```bash
# Error: Permission denied
# Solution: Make scripts executable
chmod +x scripts/*.sh
```

**Database Connection Issues**
```bash
# Check if MySQL is running
docker-compose ps

# Start database if needed
docker-compose up -d mysql
```

### Debug Mode
Run scripts with verbose output:
```bash
bash -x ./scripts/populate-database.sh
```

## Integration with CI/CD

These scripts can be integrated into your CI/CD pipeline:

```yaml
# Example GitHub Actions workflow
- name: Populate Test Data
  run: ./scripts/populate-database.sh

- name: Run API Tests
  run: ./scripts/test-api.sh

- name: Validate Data
  run: ./scripts/validate-data.sh
```

## Contributing

To add new tests or validations:

1. Follow the existing pattern for colored output
2. Add proper error handling
3. Update counters and summary statistics
4. Test with both success and failure scenarios
5. Update this README with new features