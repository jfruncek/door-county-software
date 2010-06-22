create table EVENTS (EVENT_ID number(19,0) not null, EVENT_DATE timestamp, title varchar2(255 char), primary key (EVENT_ID))

create table MOJO_BOOK_CHAPTER (CHAPTER_ID number(10,0) not null, NAME varchar2(255 char) not null, primary key (CHAPTER_ID))

create table MOJO_Chapter_Parameter (CHAPTER_ID number(10,0) not null, VALUE varchar2(255 char), NAME varchar2(255 char) not null, primary key (CHAPTER_ID, NAME))

create table MOJO_REPORT_SCHEDULE (SCHEDULE_ID number(10,0) not null, NAME varchar2(255 char) not null, primary key (SCHEDULE_ID))

create table MOJO_SCHEDULE_DELIVERY_METHOD (DELIVERY_METHOD_ID number(10,0) not null, DESCRIPTION varchar2(255 char), DELIVERY_METHOD_INDEX number(10,0) not null, primary key (DELIVERY_METHOD_ID, DELIVERY_METHOD_INDEX))

create table MOJO_SCHEDULE_PARAMETER (PARAMETER_ID number(10,0) not null, SCHEDULE_ID number(10,0), NAME varchar2(255 char), primary key (PARAMETER_ID))

create table MOJO_SCHEDULE_PARAMETER_VALUE (ID number(10,0) not null, VALUE varchar2(255 char) not null, primary key (ID, VALUE))

create table PERSON (PERSON_ID number(19,0) not null, age number(10,0), firstname varchar2(255 char), lastname varchar2(255 char), primary key (PERSON_ID))

create table PERSON_EVENT (PERSON_ID number(19,0) not null, EVENT_ID number(19,0) not null, primary key (PERSON_ID, EVENT_ID))

alter table MOJO_Chapter_Parameter add constraint FK524D3E1FE68BA1F6 foreign key (CHAPTER_ID) references MOJO_BOOK_CHAPTER

alter table MOJO_SCHEDULE_DELIVERY_METHOD add constraint FK484AF8BCC55A07BD foreign key (DELIVERY_METHOD_ID) references MOJO_REPORT_SCHEDULE

alter table MOJO_SCHEDULE_PARAMETER add constraint FK232144B9B7DDACD2 foreign key (SCHEDULE_ID) references MOJO_REPORT_SCHEDULE

alter table MOJO_SCHEDULE_PARAMETER_VALUE add constraint FK8F76124B2A0A7449 foreign key (ID) references MOJO_SCHEDULE_PARAMETER

alter table PERSON_EVENT add constraint FKAD91D910F96D1A45 foreign key (EVENT_ID) references EVENTS

alter table PERSON_EVENT add constraint FKAD91D9107708282F foreign key (PERSON_ID) references PERSON

create sequence hibernate_sequence

