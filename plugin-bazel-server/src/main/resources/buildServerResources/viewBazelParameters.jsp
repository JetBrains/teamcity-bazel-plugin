<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>


<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.bazel.BazelParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<div class="parameter">
    Command: <props:displayValue name="${params.commandKey}"/>

    <c:if test="${not empty propertiesBean.properties[params.targetsKey]}">
        <props:displayValue name="${params.targetsKey}"/>
    </c:if>

    <c:if test="${not empty propertiesBean.properties[params.argumentsKey]}">
        <props:displayValue name="${params.argumentsKey}"/>
    </c:if>
</div>

<c:if test="${not empty propertiesBean.properties[params.workingDirKey]}">
    <div class="parameter">
        Working directory: <props:displayValue name="${params.workingDirKey}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.toolPathKey]}">
    <div class="parameter">
        Path to bazel executable tool: <props:displayValue name="${params.toolPathKey}"/>
    </div>
</c:if>