package in.projecteka.datanotificationsubscription.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import in.projecteka.datanotificationsubscription.common.model.AuthPurposeCode;

import java.io.IOException;

public class AuthPurposeDeserializer extends JsonDeserializer<AuthPurposeCode> {
    @Override
    public AuthPurposeCode deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        if (node == null) {
            return null;
        }

        String text = node.textValue();

        if (text == null) {
            return null;
        }

        return AuthPurposeCode.fromText(text);
    }
}
