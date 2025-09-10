/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.application.apitypes.graphql;

import burp.application.apitypes.graphql.GraphQLBaseObject;
import burp.application.apitypes.graphql.GraphQLEnum;
import burp.application.apitypes.graphql.GraphQLInputObject;
import burp.application.apitypes.graphql.GraphQLInterface;
import burp.application.apitypes.graphql.GraphQLKind;
import burp.application.apitypes.graphql.GraphQLObject;
import burp.application.apitypes.graphql.GraphQLObjectField;
import burp.application.apitypes.graphql.GraphQLParseContext;
import burp.application.apitypes.graphql.GraphQLParseError;
import burp.application.apitypes.graphql.GraphQLParseResult;
import burp.application.apitypes.graphql.GraphQLScalar;
import burp.application.apitypes.graphql.GraphQLUnion;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.HashMap;

public class GraphQLIntrospectionParser {
    GraphQLParseResult parseIntrospection(String introspectionJson) {
        // 添加空值检查
        if (introspectionJson == null || introspectionJson.trim().isEmpty()) {
            return new GraphQLParseResult();
        }
        
        JsonElement mutationTypeJson;
        JsonObject introspection;
        try {
            // 使用更健壮的 JSON 解析方式，设置 lenient 模式
            JsonReader reader = new JsonReader(new StringReader(introspectionJson));
            reader.setLenient(true);
            introspection = (JsonObject) JsonParser.parseReader(reader);
        } catch (Exception e) {
            System.err.println("Error parsing GraphQL introspection JSON: " + e.getMessage());
            return new GraphQLParseResult();
        }
        
        // 添加空值检查
        if (introspection == null || !introspection.has("data") || introspection.get("data").isJsonNull()) {
            return new GraphQLParseResult();
        }
        
        JsonObject data = introspection.getAsJsonObject("data");
        if (data == null || !data.has("__schema") || data.get("__schema").isJsonNull()) {
            return new GraphQLParseResult();
        }
        
        introspection = data.getAsJsonObject("__schema");
        JsonArray types = introspection.getAsJsonArray("types");
        HashMap<String, GraphQLBaseObject> globalObjects = new HashMap<String, GraphQLBaseObject>();
        GraphQLParseContext context = new GraphQLParseContext(globalObjects);
        block12:
        for (JsonElement element : types) {
            // 添加空值检查
            if (element == null || !element.isJsonObject()) {
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            // 添加空值检查
            if (!object.has("kind") || !object.has("name")) {
                continue;
            }
            GraphQLKind objectKind = GraphQLKind.valueOf(object.getAsJsonPrimitive("kind").getAsString());
            String name = object.getAsJsonPrimitive("name").getAsString();
            switch (objectKind) {
                case OBJECT: {
                    globalObjects.put(name, new GraphQLObject(object));
                    continue block12;
                }
                case INTERFACE: {
                    globalObjects.put(name, new GraphQLInterface(object));
                    continue block12;
                }
                case SCALAR: {
                    globalObjects.put(name, new GraphQLScalar(name));
                    continue block12;
                }
                case ENUM: {
                    globalObjects.put(name, new GraphQLEnum(object));
                    continue block12;
                }
                case UNION: {
                    globalObjects.put(name, new GraphQLUnion(object));
                    continue block12;
                }
                case INPUT_OBJECT: {
                    globalObjects.put(name, new GraphQLInputObject(object));
                    continue block12;
                }
            }
            System.out.println((Object) objectKind);
        }
        GraphQLParseResult result = new GraphQLParseResult();
        result.queryParseResult.put("query", "{\n__schema {\nqueryType { name }\nmutationType { name }\nsubscriptionType { name }\ntypes {\n...FullType\n}\ndirectives {\nname\ndescription\nargs {\n...InputValue\n}\n}\n}\n}\n\nfragment FullType on __Type {\nkind\nname\ndescription\nfields(includeDeprecated: true) {\nname\ndescription\nargs {\n...InputValue\n}\ntype {\n...TypeRef\n}\nisDeprecated\ndeprecationReason\n}\ninputFields {\n...InputValue\n}\ninterfaces {\n...TypeRef\n}\nenumValues(includeDeprecated: true) {\nname\ndescription\nisDeprecated\ndeprecationReason\n}\npossibleTypes {\n...TypeRef\n}\n}\n\nfragment InputValue on __InputValue {\nname\ndescription\ntype {\n...TypeRef\n}\ndefaultValue\n}\n\nfragment TypeRef on __Type {\nkind\nname\nofType {\nkind\nname\nofType {\nkind\nname\nofType {\nkind\nname\nofType {\nkind\nname\nofType {\nkind\nname\nofType {\nkind\nname\n}\n}\n}\n}\n}\n}\n}\n}");
        return result;
    }
}

