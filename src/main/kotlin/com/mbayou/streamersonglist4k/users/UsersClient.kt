package com.mbayou.streamersonglist4k.users

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.ApiClient
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication
import java.net.http.HttpClient

class UsersClient(
    httpClient: HttpClient,
    mapper: ObjectMapper,
    configuration: StreamerSonglistConfiguration,
    defaultAuthentication: StreamerSonglistAuthentication?,
) : ApiClient(httpClient, mapper, configuration, defaultAuthentication) {
    fun self(authentication: StreamerSonglistAuthentication? = null): UserResponseBody {
        return get("/users/self")
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }
}
