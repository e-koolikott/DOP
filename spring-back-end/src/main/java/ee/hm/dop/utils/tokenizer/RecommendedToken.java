package ee.hm.dop.utils.tokenizer;

public class RecommendedToken extends DOPToken {

    public RecommendedToken(String content) {
        super(content);
    }

    @Override
    public String toString() {
        return "recommended:\"" + getEscapedContent() + "\"";
    }
}
