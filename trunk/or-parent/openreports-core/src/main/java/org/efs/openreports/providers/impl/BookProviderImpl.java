package org.efs.openreports.providers.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.efs.openreports.objects.ReportBook;
import org.efs.openreports.objects.ReportGroup;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.HibernateProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.ConstraintException;
import org.efs.openreports.util.LocalStrings;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class BookProviderImpl implements BookProvider {

	protected static Logger log = Logger.getLogger(BookProviderImpl.class);

	private HibernateProvider hibernateProvider;

	public BookProviderImpl(HibernateProvider hibernateProvider) {
		this.hibernateProvider = hibernateProvider;
		log.info("BookProviderImpl created");
	}

	@Override
	public ReportBook getReportBook(Integer id) throws ProviderException {
		return (ReportBook) hibernateProvider.load(ReportBook.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ReportBook> getReportBooks() throws ProviderException {
		String fromClause = "from org.efs.openreports.objects.ReportBook reportBook order by reportBook.name ";

		return (List<ReportBook>) hibernateProvider.query(fromClause);
	}

	@Override
	public ReportBook insertReportBook(ReportBook reportBook, String updateUser)
			throws ProviderException {
		return (ReportBook) hibernateProvider.saveCtrlDataObject(reportBook, updateUser);
	}

	@Override
	public void updateReportBook(ReportBook reportBook, String updateUser)
			throws ProviderException {
		hibernateProvider.updateCtrlDataObject(reportBook, updateUser);
	}

	@Override
	public void deleteReportBook(ReportBook reportBook)
			throws ProviderException {
		try {
			hibernateProvider.delete(reportBook);
		} catch (ConstraintException e) {
			throw new ProviderException(LocalStrings.ERROR_BOOK_DELETION);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ReportBook> getBooksForGroup(ReportGroup group)
			throws ProviderException {
		try {
			Session session = hibernateProvider.openSession();

			try {
				List<ReportBook> list = session.createQuery(
						"from org.efs.openreports.objects.ReportBook as reportBook "
								+ "where ? in elements(reportBook.groups)")
						.setEntity(0, group).list();

				if (list.size() == 0)
					return null;

				return list;
			} catch (HibernateException he) {
				throw he;
			} finally {
				session.close();
			}
		} catch (HibernateException he) {
			throw new ProviderException(he);
		}
	}
}
