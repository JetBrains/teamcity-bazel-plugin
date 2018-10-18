<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>
<jsp:useBean id="bean" class="jetbrains.buildServer.bazel.BazelParametersProvider"/>

<tr>
  <td colspan="2">
    <em>This build feature specifies options for Bazel<bs:help urlPrefix="https://www.bazel.build/" file=""/> build steps</em>
  </td>
</tr>

<tr>
  <th>
    <label for="${bean.remoteCacheKey}">
      Remote cache: <bs:help urlPrefix="https://docs.bazel.build/versions/master/remote-caching.html" file=""/>
    </label>
  </th>
  <td>
    <div class="posRel">
      <props:textProperty name="${bean.remoteCacheKey}" className="longField"/>
    </div>
    <span class="error" id="error_${bean.remoteCacheKey}"></span>
    <span class="smallNote">Specify the URL of Bazel HTTP caching server.</span>
  </td>
</tr>