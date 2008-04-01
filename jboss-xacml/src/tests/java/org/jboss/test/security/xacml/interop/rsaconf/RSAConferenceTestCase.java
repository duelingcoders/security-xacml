/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.test.security.xacml.interop.rsaconf;

import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.security.xacml.core.JBossPDP;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.XACMLConstants;
import org.jboss.test.security.xacml.factories.util.XACMLTestUtil;

/**
 * A RSAConferenceTestCase.
 * 
 * @author Marcus Moyses
 * @since Mar 18, 2008
 */
public class RSAConferenceTestCase extends TestCase
{

   public void atestCreateRequestWithHL7Permissions() throws Exception
   {
      Principal doctor = new Principal()
      {
         public String getName()
         {
            return "Dr. Alice";
         }
      };

      List<String> permissions = new ArrayList<String>();
      permissions.add(Util.PERMISSION_BASE + "prd-003");
      permissions.add(Util.PERMISSION_BASE + "prd-005");
      permissions.add(Util.PERMISSION_BASE + "prd-006");
      permissions.add(Util.PERMISSION_BASE + "prd-009");
      permissions.add(Util.PERMISSION_BASE + "prd-010");
      permissions.add(Util.PERMISSION_BASE + "prd-012");
      permissions.add(Util.PERMISSION_BASE + "prd-017");

      String patient = "Anthony Gurrola";

      List<String> confidentialityCodes = new ArrayList<String>();
      confidentialityCodes.add("CDA");
      confidentialityCodes.add("N");

      List<String> consentedIds = new ArrayList<String>();
      consentedIds.add("Dr. Alice");

      String resourceType = Util.MEDICAL_RECORD;

      List<String> resourcePermissions = new ArrayList<String>();
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-003");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-005");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-006");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-009");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-010");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-012");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-017");

      RequestContext request = Util.createRequestWithHL7Permissions(doctor, permissions, patient,
            confidentialityCodes, consentedIds, resourceType, resourcePermissions);

      request.marshall(System.out);
   }

   public void atestCreateRequestWithNormalRoles() throws Exception
   {
      Principal doctor = new Principal()
      {
         public String getName()
         {
            return "Dr. Alice";
         }
      };

      List<String> roles = new ArrayList<String>();
      roles.add(Util.PHYSICIAN);

      String patient = "Anthony Gurrola";

      List<String> confidentialityCodes = new ArrayList<String>();
      confidentialityCodes.add("CDA");
      confidentialityCodes.add("U");

      List<String> consentedIds = new ArrayList<String>();
      consentedIds.add("Dr. Alice");

      String resourceType = Util.MEDICAL_RECORD;

      List<String> resourcePermissions = new ArrayList<String>();
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-003");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-005");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-006");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-009");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-010");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-012");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-017");

      RequestContext request = Util.createRequestWithNormalRoles(doctor, roles, patient,
            confidentialityCodes, consentedIds, resourceType, resourcePermissions);

      request.marshall(System.out);
   }

   public void testUseCase1_1() throws Exception
   {
      PolicyDecisionPoint pdp = getPDP();
      assertNotNull("JBossPDP is != null", pdp);

      Principal doctor = new Principal()
      {
         public String getName()
         {
            return "Dr. Alice";
         }
      };

      List<String> permissions = new ArrayList<String>();
      permissions.add(Util.PERMISSION_BASE + "prd-003");
      permissions.add(Util.PERMISSION_BASE + "prd-005");
      permissions.add(Util.PERMISSION_BASE + "prd-006");
      permissions.add(Util.PERMISSION_BASE + "prd-009");
      permissions.add(Util.PERMISSION_BASE + "prd-010");
      permissions.add(Util.PERMISSION_BASE + "prd-012");
      permissions.add(Util.PERMISSION_BASE + "prd-017");

      String patient = "Anthony Gurrola";

      List<String> confidentialityCodes = new ArrayList<String>();
      confidentialityCodes.add("UBA");

      List<String> consentedIds = new ArrayList<String>();
      consentedIds.add("Dr. Alice");

      String resourceType = Util.MEDICAL_RECORD;

      List<String> resourcePermissions = new ArrayList<String>();
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-003");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-005");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-006");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-009");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-010");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-012");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-017");

      RequestContext request = Util.createRequestWithHL7Permissions(doctor, permissions, patient,
            confidentialityCodes, consentedIds, resourceType, resourcePermissions);

      assertEquals("Permit?", XACMLConstants.DECISION_PERMIT, XACMLTestUtil.getDecision(pdp,
            request));
   }

   public void atestUseCase1_2() throws Exception
   {
      PolicyDecisionPoint pdp = getPDP();
      assertNotNull("JBossPDP is != null", pdp);

      Principal doctor = new Principal()
      {
         public String getName()
         {
            return "Dr. Alice";
         }
      };

      List<String> permissions = new ArrayList<String>();

      String patient = "Anthony Gurrola";

      List<String> confidentialityCodes = new ArrayList<String>();
      confidentialityCodes.add("CDA");
      confidentialityCodes.add("N");

      List<String> consentedIds = new ArrayList<String>();
      consentedIds.add("Dr. Alice");

      String resourceType = Util.MEDICAL_RECORD;

      List<String> resourcePermissions = new ArrayList<String>();
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-003");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-005");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-006");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-009");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-010");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-012");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-017");

      RequestContext request = Util.createRequestWithHL7Permissions(doctor, permissions, patient,
            confidentialityCodes, consentedIds, resourceType, resourcePermissions);

      assertEquals("Deny?", XACMLConstants.DECISION_DENY, XACMLTestUtil.getDecision(pdp, request));
   }

   public void atestUseCase1_3() throws Exception
   {
      PolicyDecisionPoint pdp = getPDP();
      assertNotNull("JBossPDP is != null", pdp);

      Principal doctor = new Principal()
      {
         public String getName()
         {
            return "Dr. Alice";
         }
      };

      List<String> roles = new ArrayList<String>();
      roles.add(Util.PHYSICIAN);

      String patient = "Anthony Gurrola";

      List<String> confidentialityCodes = new ArrayList<String>();
      confidentialityCodes.add("UBA");
      confidentialityCodes.add("MA");

      List<String> consentedIds = new ArrayList<String>();
      consentedIds.add("Dr. Alice");

      String resourceType = Util.MEDICAL_RECORD;

      List<String> resourcePermissions = new ArrayList<String>();
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-003");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-005");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-006");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-009");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-010");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-012");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-017");

      RequestContext request = Util.createRequestWithNormalRoles(doctor, roles, patient,
            confidentialityCodes, consentedIds, resourceType, resourcePermissions);

      assertEquals("Permit?", XACMLConstants.DECISION_PERMIT, XACMLTestUtil.getDecision(pdp,
            request));
   }

   public void atestUseCase1_4() throws Exception
   {
      PolicyDecisionPoint pdp = getPDP();
      assertNotNull("JBossPDP is != null", pdp);

      Principal doctor = new Principal()
      {
         public String getName()
         {
            return "Dr. Alice";
         }
      };

      List<String> roles = new ArrayList<String>();
      roles.add("lala");

      String patient = "Anthony Gurrola";

      List<String> confidentialityCodes = new ArrayList<String>();
      confidentialityCodes.add("UBA");
      confidentialityCodes.add("MA");

      List<String> consentedIds = new ArrayList<String>();
      consentedIds.add("Dr. Alice");

      String resourceType = Util.MEDICAL_RECORD;

      List<String> resourcePermissions = new ArrayList<String>();
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-003");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-005");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-006");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-009");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-010");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-012");
      resourcePermissions.add(Util.PERMISSION_BASE + "prd-017");

      RequestContext request = Util.createRequestWithNormalRoles(doctor, roles, patient,
            confidentialityCodes, consentedIds, resourceType, resourcePermissions);

      assertEquals("Permit?", XACMLConstants.DECISION_PERMIT, XACMLTestUtil.getDecision(pdp,
            request));
   }

   private PolicyDecisionPoint getPDP()
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream is = tcl.getResourceAsStream("test/config/rsaConferencePolicySetConfig.xml");
      assertNotNull("InputStream != null", is);

      return new JBossPDP(is);
   }
}