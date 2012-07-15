package com.terradue.warhol.auth.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

final class RelaxedTrustManager
    implements X509TrustManager
{

    @Override
    public void checkClientTrusted( X509Certificate[] chain, String authType )
        throws CertificateException
    {
        // do nothing
    }

    @Override
    public void checkServerTrusted( X509Certificate[] chain, String authType )
        throws CertificateException
    {
        // do nothing
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return new X509Certificate[0];
    }

}
