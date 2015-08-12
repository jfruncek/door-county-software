# Install Perl DBI and DBD::Oracle #

```
$ perl -MCPAN -e shell
cpan[1]> install 'DBI'
cpan[2]> install 'DBD::Oracle' *failed- see session.txt*

Verify:
$ perl -e 'use DBI; print $DBI::VERSION,"\n";'
1.616
$ perl -e 'use DBD::Oracle; print $DBD::Oracle::VERSION,"\n";'
*had failed*
```

For now going back to Teramedica::DataAccess - saved replace using DBI.pl