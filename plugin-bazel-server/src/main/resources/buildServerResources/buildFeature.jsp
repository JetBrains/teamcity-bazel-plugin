<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%--
  ~ Copyright 2000-2023 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>
<jsp:useBean id="params" class="jetbrains.buildServer.bazel.BazelParametersProvider"/>

<tr>
  <td colspan="2">
    <em>This build feature specifies common options for Bazel<bs:help urlPrefix="https://www.bazel.build/" file=""/> build steps</em>
  </td>
</tr>

<tr>
    <th><label for="${params.argumentsKey}">
        Startup options:<bs:help urlPrefix="https://docs.bazel.build/versions/master/command-line-reference.html#startup-options" file=""/>
    </label></th>
    <td>
        <props:textProperty name="${params.startupOptionsKey}" className="longField" expandable="true"/>
        <span class="error" id="error_${params.startupOptionsKey}"></span>
        <span class="smallNote">Enter additional options that appear before the commands.</span>
    </td>
</tr>

<tr>
  <th>
      <label for="${params.remoteCacheKey}">
          Remote cache:<bs:help urlPrefix="https://docs.bazel.build/versions/master/remote-caching.html" file=""/>
      </label>
  </th>
  <td>
    <div class="posRel">
      <props:textProperty name="${params.remoteCacheKey}" className="longField"/>
    </div>
    <span class="error" id="error_${params.remoteCacheKey}"></span>
    <span class="smallNote">Specify the URL of Bazel HTTP caching server. Besides http, the following protocols are also supported: https, grpc, grpcs.</span>
  </td>
</tr>