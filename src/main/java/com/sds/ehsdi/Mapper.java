package com.sds.ehsdi;

import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public SOSIToken map(ParsedData parsedData) {
        // map data to SOSIToken
        SOSIToken sosiToken = new SOSIToken();

        // Map basic fields
        sosiToken.setId(parsedData.getId());
        sosiToken.setIssueInstant(parsedData.getIssueInstant());
        sosiToken.setVersion(parsedData.getVersion());
        sosiToken.setIssuer(parsedData.getIssuer());

        // Map Signature
        SOSIToken.Signature signature = new SOSIToken.Signature();
        ParsedData.Signature parsedSignature = parsedData.getSignature();
        if (parsedSignature != null) {
            signature.setSignatureMethodAlgorithm(parsedSignature.getSignatureMethodAlgorithm());
            signature.setDigestMethodAlgorithm(parsedSignature.getDigestMethodAlgorithm());
            signature.setDigestValue(parsedSignature.getDigestValue());
            signature.setSignatureValue(parsedSignature.getSignatureValue());
            signature.setCertificate(parsedSignature.getCertificate());
        }
        sosiToken.setSignature(signature);

        // Map Subject
        SOSIToken.Subject subject = new SOSIToken.Subject();
        ParsedData.Subject parsedSubject = parsedData.getSubject();
        if (parsedSubject != null) {
            subject.setNameIdFormat(parsedSubject.getNameIdFormat());
            subject.setNameIdValue(parsedSubject.getNameIdValue());
            subject.setConfirmationMethod(parsedSubject.getConfirmationMethod());
        }
        sosiToken.setSubject(subject);

        // Map Conditions
        SOSIToken.Conditions conditions = new SOSIToken.Conditions();
        ParsedData.Conditions parsedConditions = parsedData.getConditions();
        if (parsedConditions != null) {
            conditions.setNotOnOrAfter(parsedConditions.getNotOnOrAfter());
            List<String> audiences = new ArrayList<>();
            audiences.add("https://sts.sosi.dk/"); // Example: Add fixed audience
            conditions.setAudiences(audiences);
        }
        sosiToken.setConditions(conditions);

        // Map Attributes
        List<SOSIToken.Attribute> attributes = new ArrayList<>();
        for (ParsedData.Attribute parsedAttribute : parsedData.getAttributes()) {
            SOSIToken.Attribute attribute = new SOSIToken.Attribute();
            attribute.setFriendlyName(parsedAttribute.getFriendlyName());
            attribute.setName(parsedAttribute.getName());
            attribute.setValues(parsedAttribute.getValues());
            attributes.add(attribute);
        }
        sosiToken.setAttributes(attributes);

        // Add Permissions as Attributes
        List<String> permissions = parsedData.getPermissions();
        if (permissions != null && !permissions.isEmpty()) {
            SOSIToken.Attribute permissionsAttribute = new SOSIToken.Attribute();
            permissionsAttribute.setFriendlyName("XSPA permissions");
            permissionsAttribute.setName("urn:oasis:names:tc:xspa:1.0:subject:hl7:permission");
            permissionsAttribute.setValues(permissions);
            attributes.add(permissionsAttribute);
        }

        return sosiToken;
    }
}
