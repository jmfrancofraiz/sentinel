#!/bin/bash

# Sentinel Safety Monitor Build Script
# This script provides common build and development commands

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command_exists java; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    if ! command_exists adb; then
        print_warning "ADB is not installed or not in PATH. Device testing will not be available."
    fi
    
    if [ ! -f "local.properties" ]; then
        print_warning "local.properties not found. Please create it with your Android SDK path."
        print_status "Copy local.properties.template to local.properties and update the SDK path."
    fi
    
    print_success "Prerequisites check completed"
}

# Function to clean project
clean_project() {
    print_status "Cleaning project..."
    ./gradlew clean
    print_success "Project cleaned"
}

# Function to build debug APK
build_debug() {
    print_status "Building debug APK..."
    ./gradlew assembleDebug
    print_success "Debug APK built successfully"
    print_status "APK location: app/build/outputs/apk/debug/app-debug.apk"
}

# Function to build release APK
build_release() {
    print_status "Building release APK..."
    ./gradlew assembleRelease
    print_success "Release APK built successfully"
    print_status "APK location: app/build/outputs/apk/release/app-release.apk"
}

# Function to run tests
run_tests() {
    print_status "Running tests..."
    ./gradlew test
    print_success "Tests completed"
}

# Function to run instrumented tests
run_instrumented_tests() {
    print_status "Running instrumented tests..."
    ./gradlew connectedAndroidTest
    print_success "Instrumented tests completed"
}

# Function to install debug APK
install_debug() {
    print_status "Installing debug APK..."
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    print_success "Debug APK installed"
}

# Function to uninstall app
uninstall_app() {
    print_status "Uninstalling app..."
    adb uninstall com.sentinel
    print_success "App uninstalled"
}

# Function to monitor app logs in real-time
monitor_logs() {
    print_status "Starting real-time log monitoring..."
    print_status "Monitoring WhatsApp Sentinel logs with timestamps"
    print_status "Press Ctrl+C to stop monitoring"
    echo ""
    
    # Check if device is connected
    if ! adb devices | grep -q "device$"; then
        print_error "No device connected. Please connect your device and enable USB debugging."
        exit 1
    fi
    
    # Start monitoring with proper escaping for shell
    adb logcat -v time -s 'WhatsAppMonitoringService' 'ContactDetector' 'ValidationEngine' 'UIElementProcessor' 'ContextProcessor' 'InferenceEngine' 'ModelManager' 'ChatScreenIdentifier' 'WhatsAppDetector' 'LoggingManager'
}

# Function to monitor all app logs (broader filter)
monitor_all_logs() {
    print_status "Starting comprehensive log monitoring..."
    print_status "Monitoring all WhatsApp Sentinel related logs"
    print_status "Press Ctrl+C to stop monitoring"
    echo ""
    
    # Check if device is connected
    if ! adb devices | grep -q "device$"; then
        print_error "No device connected. Please connect your device and enable USB debugging."
        exit 1
    fi
    
    # Monitor with grep filter
    adb logcat | grep -i "sentinel"
}

# Function to save logs to file
save_logs() {
    local log_file="sentinel_logs_$(date +%Y%m%d_%H%M%S).txt"
    print_status "Saving logs to: $log_file"
    print_status "Press Ctrl+C to stop and save logs"
    echo ""
    
    # Check if device is connected
    if ! adb devices | grep -q "device$"; then
        print_error "No device connected. Please connect your device and enable USB debugging."
        exit 1
    fi
    
    # Save logs with timestamps
    adb logcat -v time -s 'WhatsAppMonitoringService' 'ContactDetector' 'ValidationEngine' 'UIElementProcessor' 'ContextProcessor' 'InferenceEngine' 'ModelManager' 'ChatScreenIdentifier' 'WhatsAppDetector' 'LoggingManager' > "$log_file"
    
    print_success "Logs saved to: $log_file"
}

# Function to clear logs and start fresh monitoring
clear_and_monitor() {
    print_status "Clearing existing logs..."
    adb logcat -c
    print_success "Logs cleared"
    
    print_status "Starting fresh log monitoring..."
    monitor_logs
}

# Function to show help
show_help() {
    echo "Sentinel Safety Monitor Build Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Build Commands:"
    echo "  clean           Clean the project"
    echo "  debug           Build debug APK"
    echo "  release         Build release APK"
    echo "  test            Run unit tests"
    echo "  test-instr      Run instrumented tests"
    echo "  install         Install debug APK to connected device"
    echo "  uninstall       Uninstall app from connected device"
    echo ""
    echo "Monitoring Commands:"
    echo "  logs            Monitor app logs in real-time (recommended)"
    echo "  logs-all        Monitor all app-related logs (broader filter)"
    echo "  logs-save       Save logs to timestamped file"
    echo "  logs-clear      Clear logs and start fresh monitoring"
    echo ""
    echo "Utility Commands:"
    echo "  check           Check prerequisites"
    echo "  help            Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 clean debug install    # Clean, build debug, and install"
    echo "  $0 logs                   # Monitor app logs in real-time"
    echo "  $0 logs-save              # Save logs to file"
    echo "  $0 test                   # Run tests only"
}

# Main script logic
case "${1:-help}" in
    clean)
        check_prerequisites
        clean_project
        ;;
    debug)
        check_prerequisites
        clean_project
        build_debug
        ;;
    release)
        check_prerequisites
        clean_project
        build_release
        ;;
    test)
        check_prerequisites
        run_tests
        ;;
    test-instr)
        check_prerequisites
        run_instrumented_tests
        ;;
    install)
        check_prerequisites
        install_debug
        ;;
    uninstall)
        check_prerequisites
        uninstall_app
        ;;
    logs)
        check_prerequisites
        monitor_logs
        ;;
    logs-all)
        check_prerequisites
        monitor_all_logs
        ;;
    logs-save)
        check_prerequisites
        save_logs
        ;;
    logs-clear)
        check_prerequisites
        clear_and_monitor
        ;;
    check)
        check_prerequisites
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
