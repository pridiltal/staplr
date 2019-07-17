/*
 * Copyright 2004 by Paulo Soares.
 *
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 *
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package pdftk.com.lowagie.text.pdf;

import java.security.cert.Certificate;
import java.security.cert.CRL;
import java.security.PrivateKey;
import pdftk.com.lowagie.text.ExceptionConverter;
import java.io.ByteArrayOutputStream;

/**
 * A signature dictionary representation for the standard filters.
 */
public abstract class PdfSigGenericPKCS extends PdfSignature {
    /**
     * The hash algorith, for example "SHA1"
     */    
    protected String hashAlgorithm;
    /**
     * The crypto provider
     */    
    protected String provider = null;
    /**
     * The class instance that calculates the PKCS#1 and PKCS#7
     */    
    protected PdfPKCS7 pkcs;
    /**
     * The subject name in the signing certificate (the element "CN")
     */    
    protected String   name;

    private byte externalDigest[];
    private byte externalRSAdata[];
    private String digestEncryptionAlgorithm;

    /**
     * Creates a generic standard filter.
     * @param filter the filter name
     * @param subFilter the sub-filter name
     */    
    public PdfSigGenericPKCS(PdfName filter, PdfName subFilter) {
        super(filter, subFilter);
    }

    /**
     * Sets the crypto information to sign.
     * @param privKey the private key
     * @param certChain the certificate chain
     * @param crlList the certificate revocation list. It can be <CODE>null</CODE>
     */    
    public void setSignInfo(PrivateKey privKey, Certificate[] certChain, CRL[] crlList) {
        try {
            pkcs = new PdfPKCS7(privKey, certChain, crlList, hashAlgorithm, provider, PdfName.ADBE_PKCS7_SHA1.equals(get(PdfName.SUBFILTER)));
            pkcs.setExternalDigest(externalDigest, externalRSAdata, digestEncryptionAlgorithm);
            if (PdfName.ADBE_X509_RSA_SHA1.equals(get(PdfName.SUBFILTER))) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                for (int k = 0; k < certChain.length; ++k) {
                    bout.write(certChain[k].getEncoded());
                }
                bout.close();
                setCert(bout.toByteArray());
                setContents(pkcs.getEncodedPKCS1());
            }
            else
                setContents(pkcs.getEncodedPKCS7());
            name = PdfPKCS7.getSubjectFields(pkcs.getSigningCertificate()).getField("CN");
            if (name != null)
                put(PdfName.NAME, new PdfString(name, PdfObject.TEXT_UNICODE));
            pkcs = new PdfPKCS7(privKey, certChain, crlList, hashAlgorithm, provider, PdfName.ADBE_PKCS7_SHA1.equals(get(PdfName.SUBFILTER)));
            pkcs.setExternalDigest(externalDigest, externalRSAdata, digestEncryptionAlgorithm);
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Sets the digest/signature to an external calculated value.
     * @param digest the digest. This is the actual signature
     * @param RSAdata the extra data that goes into the data tag in PKCS#7
     * @param digestEncryptionAlgorithm the encryption algorithm. It may must be <CODE>null</CODE> if the <CODE>digest</CODE>
     * is also <CODE>null</CODE>. If the <CODE>digest</CODE> is not <CODE>null</CODE>
     * then it may be "RSA" or "DSA"
     */    
    public void setExternalDigest(byte digest[], byte RSAdata[], String digestEncryptionAlgorithm) {
        externalDigest = digest;
        externalRSAdata = RSAdata;
        this.digestEncryptionAlgorithm = digestEncryptionAlgorithm;
    }

    /**
     * Gets the subject name in the signing certificate (the element "CN")
     * @return the subject name in the signing certificate (the element "CN")
     */    
    public String getName() {
        return name;
    }

    /**
     * Gets the class instance that does the actual signing.
     * @return the class instance that does the actual signing
     */    
    public PdfPKCS7 getSigner() {
        return pkcs;
    }

    /**
     * Gets the signature content. This can be a PKCS#1 or a PKCS#7. It corresponds to
     * the /Contents key.
     * @return the signature content
     */    
    public byte[] getSignerContents() {
        if (PdfName.ADBE_X509_RSA_SHA1.equals(get(PdfName.SUBFILTER)))
            return pkcs.getEncodedPKCS1();
        else
            return pkcs.getEncodedPKCS7();
    }

    /**
     * Creates a standard filter of the type VeriSign.
     */    
    public static class VeriSign extends PdfSigGenericPKCS {
        /**
         * The constructor for the default provider.
         */        
        public VeriSign() {
            super(PdfName.VERISIGN_PPKVS, PdfName.ADBE_PKCS7_DETACHED);
            hashAlgorithm = "MD5";
            put(PdfName.R, new PdfNumber(65537));
        }

        /**
         * The constructor for an explicit provider.
         * @param provider the crypto provider
         */        
        public VeriSign(String provider) {
            this();
            this.provider = provider;
        }
    }

    /**
     * Creates a standard filter of the type self signed.
     */    
    public static class PPKLite extends PdfSigGenericPKCS {
        /**
         * The constructor for the default provider.
         */        
        public PPKLite() {
            super(PdfName.ADOBE_PPKLITE, PdfName.ADBE_X509_RSA_SHA1);
            hashAlgorithm = "SHA1";
            put(PdfName.R, new PdfNumber(65541));
        }

        /**
         * The constructor for an explicit provider.
         * @param provider the crypto provider
         */        
        public PPKLite(String provider) {
            this();
            this.provider = provider;
        }
    }

    /**
     * Creates a standard filter of the type Windows Certificate.
     */    
    public static class PPKMS extends PdfSigGenericPKCS {
        /**
         * The constructor for the default provider.
         */        
        public PPKMS() {
            super(PdfName.ADOBE_PPKMS, PdfName.ADBE_PKCS7_SHA1);
            hashAlgorithm = "SHA1";
        }

        /**
         * The constructor for an explicit provider.
         * @param provider the crypto provider
         */        
        public PPKMS(String provider) {
            this();
            this.provider = provider;
        }
    }
}
