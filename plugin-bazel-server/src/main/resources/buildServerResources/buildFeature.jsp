<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>
<jsp:useBean id="params" class="jetbrains.buildServer.bazel.BazelParametersProvider"/>

<script type="text/javascript">
  BS.BazelFeature = {
    showHomePage: function() {
      var winSize = BS.Util.windowSize();
      BS.Util.popupWindow('https://www.bazel.build/', 'bazel', { width: 0.9 * winSize[0], height: 0.9 * winSize[1] });
      BS.stopPropagation(event);
    },

    showRemoteCachingPage: function() {
      var winSize = BS.Util.windowSize();
      BS.Util.popupWindow('https://docs.bazel.build/versions/master/remote-caching.html', 'bazel', {width: 0.9 * winSize[0], height: 0.9 * winSize[1]});
      BS.stopPropagation(event);
    }
  }
</script>


<tr>
  <td colspan="2"><em>This build feature specifies common options for bazel<a class='helpIcon' onclick='BS.BazelFeature.showHomePage()' title='View help'><i class='icon icon16 tc-icon_help_small'></i></a> build steps</em></td>
</tr>

<tr>
  <th><label for="${params.argumentsKey}">Startup options:</label></th>
  <td>
    <props:textProperty name="${params.startupOptionsKey}" className="longField" expandable="true"/>
    <span class="error" id="error_${params.startupOptionsKey}"></span>
    <span class="smallNote">Enter additional options that appear before the commands.</span>
  </td>
</tr>

<tr>
  <th>
    <label for="${params.remoteCacheKey}">Remote Cache:
      <a class="helpIcon" onclick="BS.BazelFeature.showRemoteCachingPage()" title="View help"><bs:helpIcon/></a>
    </label>
  </th>
  <td>
    <div class="posRel">
      <props:textProperty name="${params.remoteCacheKey}" className="longField"/>
    </div>
    <span class="error" id="error_${params.remoteCacheKey}"></span>
    <span class="smallNote">Specify the base URL of a HTTP caching service. Both http:// and https:// are supported.</span>
  </td>
</tr>