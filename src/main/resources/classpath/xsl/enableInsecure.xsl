<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" encoding="utf-8" indent="no" />

  <xsl:template match="//SecuritySetting[1]">

       <SecuritySetting>
         <SecurityPolicy>http://opcfoundation.org/UA/SecurityPolicy#None</SecurityPolicy>
         <MessageSecurityMode>None</MessageSecurityMode>
       </SecuritySetting>

    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//SecuritySetting/SecurityPolicy">
    <xsl:copy-of select="."/>

    <MessageSecurityMode>Sign</MessageSecurityMode>

  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
