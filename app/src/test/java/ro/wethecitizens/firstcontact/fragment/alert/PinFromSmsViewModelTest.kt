package ro.wethecitizens.firstcontact.fragment.alert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class PinFromSmsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var objectUnderTest: PinFromSmsViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        objectUnderTest = PinFromSmsViewModel()
    }

    @Test
    fun retrievePinFromSms() {
        val testPin = "123"
        val testExpiryTime = "6"

        val testString =
            "PIN-ul de autorizare First Contact este $testPin. Expira in $testExpiryTime min."

        assertEquals(testPin, objectUnderTest.retrievePinFromSms(testString))
    }
}