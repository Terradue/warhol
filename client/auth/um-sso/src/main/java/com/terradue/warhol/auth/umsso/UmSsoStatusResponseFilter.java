package com.terradue.warhol.auth.umsso;

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

import static com.ning.http.util.AsyncHttpProviderUtils.parseCookie;
import static com.terradue.warhol.lang.Preconditions.checkState;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.Cookie;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.ResponseFilter;
import com.terradue.warhol.settings.HttpMethod;
import com.terradue.warhol.settings.UmSsoAuthentication;
import com.terradue.warhol.settings.UmSsoParameter;

/**
 *
 */
final class UmSsoStatusResponseFilter
    implements ResponseFilter
{

    private static final String SET_COOKIE = "Set-Cookie";

    private final BitSet admittedStatuses = new BitSet();

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Set<Cookie> cookies = new HashSet<Cookie>();

    private final UmSsoAuthentication authentication;

    public UmSsoStatusResponseFilter( UmSsoAuthentication authentication )
    {
        this.authentication = authentication;

        admittedStatuses.set( HTTP_OK );
        admittedStatuses.set( HTTP_MOVED_PERM );
        admittedStatuses.set( HTTP_MOVED_TEMP );
        admittedStatuses.set( 307 ); // Temporary Redirect
    }

    @Override
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public FilterContext filter( FilterContext ctx )
        throws FilterException
    {
        HttpResponseStatus responseStatus = ctx.getResponseStatus();

        // 1. verify the state is one of the admitted
        checkState( admittedStatuses.get( responseStatus.getStatusCode() ),
                    "Impossible to query the catalog %s, server replied %s",
                    ctx.getRequest().getRawUrl(), responseStatus.getStatusText() );

        // 2. collect all the cookies
        final List<String> cookiesString = ctx.getResponseHeaders().getHeaders().get( SET_COOKIE );
        if ( cookiesString != null && !cookiesString.isEmpty() )
        {
            // collect all cookies, adding/replacing them with latest updated value
            for ( String cookieValue : cookiesString )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Received cookie {}", cookieValue );
                }

                Cookie currentCookie = parseCookie( cookieValue );

                cookies.add( currentCookie );
            }
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Checking server status code reply: {}", responseStatus.getStatusCode() );
        }

        // check UM-SSO conditions - this is hack-ish but didn't find a better solution ATM
        if ( HTTP_OK == responseStatus.getStatusCode()
             && ( contained( authentication.getLoginForm(), ctx.getRequest().getUrl() )
                  || contained( ctx.getRequest().getUrl(), authentication.getLoginForm() ) ) )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Redirecting request to {} {}", authentication.getMethod(), authentication.getLoginForm() );
            }

            RequestBuilder authRequestBuilder = new RequestBuilder( authentication.getMethod().toString() )
                                                         .setUrl( authentication.getLoginForm() );

            for ( Cookie cookie : cookies )
            {
                authRequestBuilder.addCookie( cookie );
            }

            for ( UmSsoParameter parameter : authentication.getParameter() )
            {
                authRequestBuilder.addParameter( parameter.getName(), parameter.getValue() );
            }

            return new FilterContext.FilterContextBuilder( ctx )
                                    .request( authRequestBuilder.build() )
                                    .asyncHandler( ctx.getAsyncHandler() )
                                    .replayRequest( true )
                                    .build();
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Proceeding on serving the request" );
        }

        if ( HttpMethod.POST.toString().equals( ctx.getRequest().getMethod() ) )
        {
            RequestBuilder redirect = new RequestBuilder( ctx.getRequest() ).setMethod( HttpMethod.GET.toString() );

            // reply all cookies
            for ( Cookie cookie : cookies )
            {
                redirect.addOrReplaceCookie( cookie );
            }

            return new FilterContext.FilterContextBuilder( ctx )
                                    .request( redirect.build() )
                                    .replayRequest( true )
                                    .build();
        }

        return ctx;
    }

    private static boolean contained( String what, String where )
    {
        return where.startsWith( what );
    }

}
