<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.bazel.BazelParametersProvider"/>

<tr>
    <th><label for="${params.buildTargetKey}">Target:</label></th>
    <td>
        <props:textProperty name="${params.buildTargetKey}" className="longField"/>
        <bs:vcsTree treeId="${params.buildTargetKey}" fieldId="${params.buildTargetKey}" />
        <span class="error" id="error_${params.buildTargetKey}"></span>
        <span class="smallNote">Target to build.</span>
    </td>
</tr>