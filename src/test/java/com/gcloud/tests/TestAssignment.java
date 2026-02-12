package com.gcloud.tests;

import com.gcloud.base.BaseTest;
import com.gcloud.pages.AssignmentPage;
import com.gcloud.pages.LoginPage;
import org.testng.annotations.Test;

public class TestAssignment extends BaseTest {

        @Test
        public void testAssignmentEndToEndFlow() {

                LoginPage loginPage = new LoginPage(BaseTest.getDriver());
                AssignmentPage assignmentPage = new AssignmentPage(BaseTest.getDriver());
                // Perform login and navigate to assignment
                loginPage.login(
                                BaseTest.getCredential("user_email"),
                                BaseTest.getCredential("user_password"),
                                BaseTest.getCredential("user_org"));

                // Verify dashboard loaded
                loginPage.verifyLandingPageLoaded();

                // Navigate to Assignment
                assignmentPage.navigateToAssignment();

                // Complete assignment and verify progress
                assignmentPage.completeAssignmentFlow();
        }
}
