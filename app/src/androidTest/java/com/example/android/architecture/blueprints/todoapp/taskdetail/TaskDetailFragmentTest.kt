package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.data.Task
import org.junit.Test
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.repository.FakeAndroidTestRepository
import com.example.android.architecture.blueprints.todoapp.data.repository.TasksRepository
import com.example.android.architecture.blueprints.todoapp.di.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

/*
Marks the test as a "medium run-time" integration test
(versus @SmallTest unit tests and @LargeTest end-to-end tests).
This helps you group and choose which size of test to run.
*/
@MediumTest
//Used in any class using AndroidX Test.
@RunWith(AndroidJUnit4::class)
class TaskDetailFragmentTest {
    /*
    * FragmentScenario is a class from AndroidX Test that wraps around a fragment and gives
    * you direct control over the fragment's lifecycle for testing.
    * To write tests for fragments, you create a FragmentScenario for the fragment you're testing.
    * */
    private lateinit var repository: TasksRepository

    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    @ExperimentalCoroutinesApi
    fun cleanupDb() = runBlockingTest {
        ServiceLocator.resetRepository()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun activeTaskDetails_DisplayedInUi() = runBlockingTest {
        // GIVEN - Add active (incomplete) task to the DB
        val taskTitle = "Active Task"
        val taskDescription = "AndroidX Rocks"
        val activeTask = Task(taskTitle, taskDescription, false)
        repository.saveTask(activeTask)

        // WHEN - Details fragment launched to display task
        val bundle = TaskDetailFragmentArgs(activeTask.id).toBundle()

        //The launchFragmentInContainer function creates a FragmentScenario,
        // with this bundle and a theme.

        //Note: Supplying the theme is necessary because fragments usually get their theming
        // from their parent activity. When using FragmentScenario,
        // your fragment is launched inside a generic empty activity so that
        // it's properly isolated from activity code (you are just testing the fragment code,
        // not the associated activity). The theme parameter allows you to supply the correct theme.
        launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.AppTheme)

        // THEN - Task details are displayed on the screen
        // make sure that the title/description are both shown and correct
        onView(withId(R.id.task_detail_title_text)).run {
            check(matches(isDisplayed()))
            check(matches(withText(taskTitle)))
        }
        onView(withId(R.id.task_detail_description_text)).run {
            check(matches(isDisplayed()))
            check(matches(withText(taskDescription)))
        }
        // and make sure the "complete" checkbox is shown unchecked
        onView(withId(R.id.task_detail_complete_checkbox)).run {
            check(matches(isDisplayed()))
            check(matches(isNotChecked()))
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun completedTaskDetails_DisplayedInUi() = runBlockingTest {
        // GIVEN - Add completed task to the DB
        val taskTitle = "Completed Task"
        val taskDescription = "AndroidX Rocks"
        val completedTask = Task(taskTitle, taskDescription, true)
        repository.saveTask(completedTask)
        // WHEN - Details fragment launched to display task
        val bundle = TaskDetailFragmentArgs(completedTask.id).toBundle()
        launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.AppTheme)

        // THEN - Task details are displayed on the screen
        // make sure that the title/description are both shown and correct
        onView(withId(R.id.task_detail_title_text)).run {
            check(matches(isDisplayed()))
            check(matches(withText(taskTitle)))
        }
        onView(withId(R.id.task_detail_description_text)).run {
            check(matches(isDisplayed()))
            check(matches(withText(taskDescription)))
        }
        // and make sure the "complete" checkbox is shown checked
        onView(withId(R.id.task_detail_complete_checkbox)).run {
            check(matches(isDisplayed()))
            check(matches(isChecked()))
        }
    }
}