<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.bazel.BazelParametersProvider"/>

<tr>
    <th><label for="${params.buildTargetsKey}">Targets:</label></th>
    <td>
        <props:textProperty name="${params.buildTargetsKey}" className="longField"/>
        <bs:projectData type="BazelTargets" sourceFieldId="${params.workingDirKey}"
                        targetFieldId="${params.buildTargetsKey}" popupTitle="Select targets"
                        selectionMode="multiple"/>
        <span class="error" id="error_${params.buildTargetsKey}"></span>
        <span class="smallNote">Enter the list of build targets.</span>
    </td>
</tr>