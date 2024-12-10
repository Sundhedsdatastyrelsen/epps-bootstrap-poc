package com.sds.ehsdi;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class ParsedData {

    private String id;                              // ID attribute of the Assertion element
    private String issueInstant;                    // IssueInstant attribute
    private String version;                         // Version attribute
    private String issuer;                          // Issuer value
    private Signature signature;                    // Nested Signature structure
    private Subject subject;                        // Nested Subject structure
    private Conditions conditions;                  // Nested Conditions structure
    private List<Attribute> attributes;             // List of Attributes from AttributeStatement
    private List<String> permissions;               // List of permission AttributeValues

    @Data
    @NoArgsConstructor
    public static class Signature {
        private String signatureMethodAlgorithm;    // SignatureMethod Algorithm
        private String digestMethodAlgorithm;       // DigestMethod Algorithm
        private String digestValue;                 // DigestValue
        private String signatureValue;              // SignatureValue
        private String certificate;                 // X509Certificate
    }

    @Data
    @NoArgsConstructor
    public static class Subject {
        private String nameIdFormat;                // NameID Format attribute
        private String nameIdValue;                 // NameID value
        private String confirmationMethod;          // SubjectConfirmation Method
    }

    @Data
    @NoArgsConstructor
    public static class Conditions {
        private String notBefore;                   // NotBefore attribute
        private String notOnOrAfter;                // NotOnOrAfter attribute
    }

    @Data
    @NoArgsConstructor
    public static class Attribute {
        private String friendlyName;                // FriendlyName attribute
        private String name;                        // Name attribute
        private List<String> values;                // List of AttributeValue elements
    }
}
