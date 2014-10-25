package de.triplet.gradle.play

import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.AppEdit
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.verify

public class PlayPublishTaskTest {

    @Mock
    AndroidPublisher publisherMock

    @Mock
    AndroidPublisher.Edits editsMock

    @Mock
    AndroidPublisher.Edits.Insert insertMock

    // AppEdit is final and not mockable
    AppEdit appEdit = new AppEdit()

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        doReturn(editsMock).when(publisherMock).edits()
        doReturn(insertMock).when(editsMock).insert(anyString(), any(AppEdit.class))
        doReturn(appEdit).when(insertMock).execute()
    }

    @Test
    public void testApplicationId() {
        Project project = TestHelper.evaluatableProject();
        project.evaluate()

        // Attach the mock
        project.tasks.publishApkRelease.service = publisherMock

        // run predecessors
        project.tasks.preBuild.execute()
        project.tasks.processReleaseManifest.execute()
        project.tasks.generateReleasePlayResources.execute()

        // finally run the task we want to check
        project.tasks.publishApkRelease.publish()

        // verify that we init the connection with the correct application id
        verify(editsMock).insert("com.example.publisher", null)
    }

    @Test
    public void testApplicationIdWithFlavors() {
        Project project = TestHelper.evaluatableProject();

        project.android {
            productFlavors {
                paid {
                    applicationId 'com.example.publisher.paid'
                }
                free
            }
        }

        project.evaluate()

        // Attach the mock
        project.tasks.publishApkPaidRelease.service = publisherMock

        // run predecessors
        project.tasks.preBuild.execute()
        project.tasks.processPaidReleaseManifest.execute()
        project.tasks.generatePaidReleasePlayResources.execute()

        // finally run the task we want to check
        project.tasks.publishApkPaidRelease.publish()

        // verify that we init the connection with the correct application id
        verify(editsMock).insert("com.example.publisher.paid", null)
    }

    @Test
    public void testMatcher() {
        Project project = TestHelper.evaluatableProject();
        project.evaluate()

        Pattern pattern = project.tasks.publishApkRelease.matcher

        Matcher m = pattern.matcher("de-DE")
        assertTrue(m.find())

        m = pattern.matcher("de")
        assertTrue(m.find())

        m = pattern.matcher("es-419")
        assertTrue(m.find())

        m = pattern.matcher("de_DE")
        assertFalse(m.find())
    }
}