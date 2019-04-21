-- Run this once before building project if using Oracle
alter session set "_ORACLE_SCRIPT"=true;

-- create new user
CREATE USER OT IDENTIFIED BY testtest;

-- grant priviledges
GRANT CONNECT, RESOURCE, DBA TO OT;
