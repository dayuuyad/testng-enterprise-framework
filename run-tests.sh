#!/bin/bash

# TestNG Enterprise Framework - Test Runner Script

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Default configuration
DEFAULT_ENVIRONMENT="qa"
DEFAULT_SUITE="smoke-test.xml"
DEFAULT_BROWSER="chrome"
DEFAULT_THREADS="3"

# Variables
ENVIRONMENT=$DEFAULT_ENVIRONMENT
SUITE=$DEFAULT_SUITE
BROWSER=$DEFAULT_BROWSER
THREADS=$DEFAULT_THREADS
HEADLESS="false"
PARALLEL="false"
GROUPS=""
EXCLUDE_GROUPS=""
CLEAN_BUILD="false"
DRY_RUN="false"

# Print banner
print_banner() {
    echo -e "${CYAN}"
    echo "╔══════════════════════════════════════════════════════════╗"
    echo "║          TESTNG ENTERPRISE TEST FRAMEWORK                ║"
    echo "║                   Version: 1.0.0                         ║"
    echo "╚══════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Print usage
print_usage() {
    echo -e "${YELLOW}Usage: $0 [OPTIONS]${NC}"
    echo ""
    echo -e "${BLUE}Options:${NC}"
    echo "  -e, --environment ENV    Test environment (dev|qa|staging|production)"
    echo "  -s, --suite FILE         TestNG XML suite file"
    echo "  -b, --browser BROWSER    Browser name (chrome|firefox|edge)"
    echo "  -p, --parallel           Enable parallel execution"
    echo "  -t, --threads NUM        Number of threads"
    echo "  -h, --headless           Run browser in headless mode"
    echo "  -g, --groups LIST        Test groups to include"
    echo "  -x, --exclude LIST       Test groups to exclude"
    echo "  -c, --clean              Clean build before execution"
    echo "  -d, --dry-run            Show what would be executed"
    echo "  --help                   Show this help"
    echo ""
    echo -e "${BLUE}Examples:${NC}"
    echo "  $0 -e qa -s smoke-test.xml"
    echo "  $0 --environment staging --suite regression-test.xml --parallel"
    echo "  $0 -e dev -g smoke,api --headless"
}

# Parse arguments
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -e|--environment)
                ENVIRONMENT="$2"
                shift 2
                ;;
            -s|--suite)
                SUITE="$2"
                shift 2
                ;;
            -b|--browser)
                BROWSER="$2"
                shift 2
                ;;
            -p|--parallel)
                PARALLEL="true"
                shift
                ;;
            -t|--threads)
                THREADS="$2"
                shift 2
                ;;
            -h|--headless)
                HEADLESS="true"
                shift
                ;;
            -g|--groups)
                GROUPS="$2"
                shift 2
                ;;
            -x|--exclude)
                EXCLUDE_GROUPS="$2"
                shift 2
                ;;
            -c|--clean)
                CLEAN_BUILD="true"
                shift
                ;;
            -d|--dry-run)
                DRY_RUN="true"
                shift
                ;;
            --help)
                print_usage
                exit 0
                ;;
            *)
                echo -e "${RED}Unknown option: $1${NC}"
                print_usage
                exit 1
                ;;
        esac
    done
}

# Validate arguments
validate_arguments() {
    # Validate environment
    case $ENVIRONMENT in
        dev|qa|staging|production)
            ;;
        *)
            echo -e "${RED}Invalid environment: $ENVIRONMENT${NC}"
            exit 1
            ;;
    esac

    # Validate suite file exists
    if [ ! -f "test-suites/$SUITE" ]; then
        echo -e "${RED}Test suite file not found: test-suites/$SUITE${NC}"
        exit 1
    fi
}

# Print configuration
print_configuration() {
    echo -e "${MAGENTA}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${MAGENTA}║                    CONFIGURATION                         ║${NC}"
    echo -e "${MAGENTA}╠══════════════════════════════════════════════════════════╣${NC}"
    echo -e "${MAGENTA}║  Environment: $ENVIRONMENT${NC}"
    echo -e "${MAGENTA}║  Test Suite: $SUITE${NC}"
    echo -e "${MAGENTA}║  Browser:    $BROWSER${NC}"
    echo -e "${MAGENTA}║  Headless:   $HEADLESS${NC}"
    echo -e "${MAGENTA}║  Parallel:   $PARALLEL${NC}"
    if [ "$PARALLEL" = "true" ]; then
        echo -e "${MAGENTA}║  Threads:    $THREADS${NC}"
    fi
    if [ -n "$GROUPS" ]; then
        echo -e "${MAGENTA}║  Groups:     $GROUPS${NC}"
    fi
    echo -e "${MAGENTA}║  Timestamp:  $(date)${NC}"
    echo -e "${MAGENTA}╚══════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

# Clean build
clean_build() {
    echo -e "${YELLOW}Cleaning build...${NC}"
    mvn clean -q
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Build cleaned${NC}"
    else
        echo -e "${RED}✗ Build clean failed${NC}"
        exit 1
    fi
}

# Run tests
run_tests() {
    echo -e "${YELLOW}Running tests...${NC}"

    # Build Maven command
    MAVEN_CMD="mvn test"
    MAVEN_CMD="$MAVEN_CMD -DsuiteXmlFile=test-suites/$SUITE"
    MAVEN_CMD="$MAVEN_CMD -Denvironment=$ENVIRONMENT"
    MAVEN_CMD="$MAVEN_CMD -Dbrowser=$BROWSER"
    MAVEN_CMD="$MAVEN_CMD -Dheadless=$HEADLESS"
    MAVEN_CMD="$MAVEN_CMD -Dparallel=$PARALLEL"
    MAVEN_CMD="$MAVEN_CMD -DthreadCount=$THREADS"

    if [ -n "$GROUPS" ]; then
        MAVEN_CMD="$MAVEN_CMD -Dgroups=$GROUPS"
    fi
    if [ -n "$EXCLUDE_GROUPS" ]; then
        MAVEN_CMD="$MAVEN_CMD -DexcludedGroups=$EXCLUDE_GROUPS"
    fi

    if [ "$DRY_RUN" = "true" ]; then
        echo -e "${CYAN}Dry run - would execute:${NC}"
        echo "$MAVEN_CMD"
        return 0
    else
        echo -e "${CYAN}Executing:${NC} $MAVEN_CMD"
        echo ""

        # Start timer
        START_TIME=$(date +%s)

        # Execute
        eval $MAVEN_CMD
        TEST_EXIT_CODE=$?

        # Calculate time
        END_TIME=$(date +%s)
        EXECUTION_TIME=$((END_TIME - START_TIME))

        return $TEST_EXIT_CODE
    fi
}

# Generate reports
generate_reports() {
    echo ""
    echo -e "${YELLOW}Generating reports...${NC}"

    if [ "$DRY_RUN" = "false" ]; then
        mvn site -q
        mkdir -p test-results/html-reports
        cp -r target/site/* test-results/html-reports/ 2>/dev/null || true
        echo -e "${GREEN}✓ Reports generated${NC}"
    fi
}

# Display report locations
display_report_locations() {
    echo ""
    echo -e "${CYAN}📊 REPORT LOCATIONS:${NC}"
    echo "  HTML Reports: test-results/html-reports/index.html"
    echo "  XML Reports:  test-results/xml-reports/"
    echo "  Screenshots:  test-results/screenshots/"
    echo ""
}

# Main function
main() {
    print_banner
    parse_arguments "$@"
    validate_arguments
    print_configuration

    if [ "$CLEAN_BUILD" = "true" ]; then
        clean_build
    fi

    run_tests
    TEST_RESULT=$?

    generate_reports
    display_report_locations

    if [ $TEST_RESULT -eq 0 ]; then
        echo -e "${GREEN}✅ Test execution completed successfully${NC}"
    else
        echo -e "${RED}❌ Test execution failed${NC}"
    fi

    exit $TEST_RESULT
}

# Run main
main "$@"
