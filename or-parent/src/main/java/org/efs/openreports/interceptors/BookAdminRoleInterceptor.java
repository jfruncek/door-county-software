package org.efs.openreports.interceptors;

import org.efs.openreports.objects.ReportUser;

public class BookAdminRoleInterceptor extends SecurityInterceptor {
	private static final long serialVersionUID = 1974710426129257550L;

	protected boolean isAuthorized(ReportUser user)
	{
		return user.isBookAdmin();
	}
}
