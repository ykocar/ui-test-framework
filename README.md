# Automated Learning Progress Test Framework

A Java-based Selenium WebDriver framework built with Maven, TestNG, and Page Object Model (POM) for automating end-to-end testing of an application. Designed for scalability, reusability, and maintainability.

## Getting Started

### Prerequisites
Java 11+
Maven 3.6+
ChromeDriver / WebDriver compatible with your browser

### Clone the Repository

git clone https://github.com/ykocar/ui-test-framework.git

### Configure Environment Settings

Navigate to: **src/main/resources**

You will find a file named:

**config.template.properties**

Rename it to: **config.properties**

***Now update it with your environment details:***

## Application Environment Configuration
- url=https://your-test-environment-url.com
- browser=chrome

- implicit_wait=10
- explicit_wait=15

### Configure Credentials (Local Only)

In the same folder, You will find a file named: 

**credentials.template.properties**

Rename it to: **credentials.properties**

Add your login details:

- user_email=
- user_password=
- user_org=

## Install Dependencies

From the project root, run:

**mvn clean install**

## Run the Tests

Run all tests:

**mvn test**

Run a specific test class:

**mvn -Dtest=TestAssignment test**

## Important Note: Resetting Module Progress

Tests may require the module to be in a fresh state before running.  

To reset a module manually:

1. Go to the **Development Page** of the application.
2. Locate the assignment row for the module you want to reset.
3. Click the **context menu (3-dots)** on the far right of the row.
4. Select the **Reset** icon to clear the module’s progress and state.

> After performing the manual reset, you can run the automated tests normally.




