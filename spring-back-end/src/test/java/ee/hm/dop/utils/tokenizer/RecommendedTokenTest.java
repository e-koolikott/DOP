package ee.hm.dop.utils.tokenizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class RecommendedTokenTest {

    private static final String DOUBLE_QUOTE = "\"";

    @Test
    public void testToString() {
        RecommendedToken token = new RecommendedToken("true");
        assertEquals("recommended:" + DOUBLE_QUOTE + "true" + DOUBLE_QUOTE, token.toString());
    }
}
