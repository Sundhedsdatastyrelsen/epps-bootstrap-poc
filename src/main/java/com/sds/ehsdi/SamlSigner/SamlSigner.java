package com.sds.ehsdi.SamlSigner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;

import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SamlSigner {
  public static void main(String[] args) throws Exception {
    try {
      if (args.length != 3) {
        System.out.println("Usage: java SAMLSignerExample <input-xml-file> <output-xml-file> <keystore-password>");
        System.exit(1);
      }

      String inputXmlPath = args[0];
      String outputXmlPath = args[1];
      String keyStrorePassword = args[2];

      // Load the PKCS12 keystore
      String p12Path = "signing-test-ncpehealth.p12";
      char[] storePassword = keyStrorePassword.toCharArray();
      String alias = "signing-test-ncpehealth";

      KeyStore ks = KeyStore.getInstance("PKCS12");

      try (FileInputStream fis = new FileInputStream(p12Path)) {
        ks.load(fis, storePassword);
      }

      PrivateKey privateKey = (PrivateKey) ks.getKey(alias, storePassword);
      X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc;
      try (FileInputStream fis = new FileInputStream(inputXmlPath)) {
        doc = db.parse(fis);
      }

      Element assertionElement = (Element) doc.getDocumentElement();
      assertionElement.setIdAttribute("ID", true);

      XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
      
      Transform envelopedTransform = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);

      Transform excC14nTransform = fac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#",
          (TransformParameterSpec) null);
      
      String assertionId = assertionElement.getAttribute("ID");

      Reference ref = fac.newReference(
          "#" + assertionId,
          fac.newDigestMethod(DigestMethod.SHA256, null),
          List.of(envelopedTransform, excC14nTransform),
          null,
          null);

      SignedInfo si = fac.newSignedInfo(
          fac.newCanonicalizationMethod(
              CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
          fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
          Collections.singletonList(ref));

      KeyInfoFactory kif = fac.getKeyInfoFactory();
      X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
      KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

      DOMSignContext dsc = new DOMSignContext(privateKey, assertionElement);
      
      XMLSignature signature = fac.newXMLSignature(si, keyInfo);

      signature.sign(dsc);

      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer t = tf.newTransformer();
      try (FileOutputStream fos = new FileOutputStream(new File(outputXmlPath))) {
        t.transform(new DOMSource(doc), new StreamResult(fos));
      }
      System.out.println("Successfully signed SAML XML. Output file: " + outputXmlPath);
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
