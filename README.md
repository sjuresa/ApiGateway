# API gateway

## Introduction

## Registration


### Get registration

Get registration data for registered RA user. Response 200 if user has RA role, else 404 Not Found.

####Post Registration

Post registration data for new / edit user. Response 201 if user has RA role, else 403 - Forbidden. If user with attribute email allready exsists, return 409 - Conflict.

## Self-registration

### Get self registration

Get registration data. Response 200.

### Post Registration

Post registration data for new user. Response 201 if ok. If user with attribute email allready exsists, return 409 - Conflict.

