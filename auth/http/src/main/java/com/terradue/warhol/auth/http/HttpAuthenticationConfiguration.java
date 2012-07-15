package com.terradue.warhol.auth.http;

/*
 *    Copyright 2011-2012 Terradue srl
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import org.kohsuke.MetaInfServices;

import com.ning.http.client.Realm;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Realm.AuthScheme;
import com.terradue.warhol.auth.AuthenticationConfiguration;
import com.terradue.warhol.settings.HttpAuthentication;

@MetaInfServices
public final class HttpAuthenticationConfiguration
    implements AuthenticationConfiguration<HttpAuthentication>
{

    @Override
    public Class<HttpAuthentication> getAuthenticationType()
    {
        return HttpAuthentication.class;
    }

    @Override
    public void configure( Builder httpClientConfig, HttpAuthentication authentication )
    {
        final Realm.RealmBuilder realmBuilder = new Realm.RealmBuilder();

        switch ( authentication.getScheme() )
        {
            case BASIC:
                realmBuilder.setScheme( AuthScheme.BASIC );
                break;

            case DIGEST:
                realmBuilder.setScheme( AuthScheme.DIGEST );
                break;

            case KERBEROS:
                realmBuilder.setScheme( AuthScheme.KERBEROS );
                break;

            case NONE:
                realmBuilder.setScheme( AuthScheme.NONE );
                break;

            case NTLM:
                realmBuilder.setScheme( AuthScheme.NTLM );
                break;

            case SPNEGO:
                realmBuilder.setScheme( AuthScheme.SPNEGO );
                break;

            default:
                // do nothing
                break;
        }

        httpClientConfig.setRealm( realmBuilder.setPrincipal( authentication.getUsername() )
                                               .setPassword( authentication.getPassword() )
                                               .setUsePreemptiveAuth( authentication.isPreemptive() )
                                               .build() );
    }

}
