# Search BLOBs #

```
select job_name, job_group, description, job_class_name 
  from openreports.qrtz_job_details t
 where dbms_lob.instr(t.job_data,utl_raw.cast_to_raw('<search text>'))>0
```

# URL examples #

```
jdbc:oracle:thin:@apple:1521/mkedev

jdbc:oracle:thin:@(description=(address_list=
      (address=(host=apple)(protocol=tcp)(port=1521))
      (address=(host=orange)(protocol=tcp)(port=1521))
      (address=(host=banana)(protocol=tcp)(port=1521))
   (load_balance=yes))(connect_data=(service_name=dev)))
```

```
jdbc:oracle:thin:@(description=(address=(protocol=TCP)(host=test)(port=1521))(connect_data=(sid=ECTEST)))
```

```
sqlplus sa/dev@EVERMORE 
```

```
sqlplus 'sa/dev@(description=(address=(protocol=TCP)(host=test)(port=1521))(connect_data=(sid=ECTEST)))'
```

# Anonymous block #

```
declare

  country_code VARCHAR2(2);
  
begin

  for rec in (select report_date, id, country
                from riskrpt.risk_sort_data 
               where report_date=20100630) loop

    select country_cd_iso2 into country_code from starkdb1.countries where country_name = rec.country;
  
    update riskrpt.risk_sort_data s
       set s.country = country_code
     where s.id = rec.id
       and s.report_date = rec.report_date;
       
    commit;
  
  end loop;

end;

```