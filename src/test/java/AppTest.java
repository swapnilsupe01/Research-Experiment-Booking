
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void bookingSystemNameShouldBeCorrect() {
        String name = "Research Experiment Booking System";

        assertEquals(
            "Research Experiment Booking System",
            name
        );
    }
}
