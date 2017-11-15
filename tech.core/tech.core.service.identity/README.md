## Verify get parent group.

### Setup

- Follow docker/README.md to setup the environment.
- Execute the following sql against the mysql database(Authorization):
alter table `group`   
Add column private_group tinyint(1) default 0 AFTER `name` ;
alter table `group`   
Add column self_register tinyint(1) default 0 AFTER `name` ;

delete from group_membership;
delete from `group`;
insert into `group` (id, name, description, createdBy, createdAt, modifiedBy, modifiedAt) values(1, 'group 1', 'group 1 desc', 1, '2017-08-22', 1, '2017-08-22');
insert into `group` (id, name, description, createdBy, createdAt, modifiedBy, modifiedAt) values(2, 'group 2', 'group 2 desc', 1, '2017-08-22', 1, '2017-08-22');
insert into `group` (id, name, description, createdBy, createdAt, modifiedBy, modifiedAt) values(3, 'group 3', 'group 3 desc', 1, '2017-08-22', 1, '2017-08-22');
insert into `group` (id, name, description, createdBy, createdAt, modifiedBy, modifiedAt) values(4, 'group 4', 'group 4 desc', 1, '2017-08-22', 1, '2017-08-22');
insert into `group` (id, name, description, createdBy, createdAt, modifiedBy, modifiedAt) values(5, 'group 5', 'group 5 desc', 1, '2017-08-22', 1, '2017-08-22');

insert into group_membership values(1, 1, 2, 2, 1, '2017-08-22', 1, '2017-08-22');
insert into group_membership values(2, 1, 3, 2, 1, '2017-08-22', 1, '2017-08-22');
insert into group_membership values(3, 2, 4, 2, 1, '2017-08-22', 1, '2017-08-22');
insert into group_membership values(4, 5, 2, 2, 1, '2017-08-22', 1, '2017-08-22');

- The group with id=4 has one parent group whose id is 2 and the group with id=2 has two parent groups whose ids are 1 and 5.


### Verify
Import the doc/groups-api.postman_collection.json into the postman, and check the test api in the Get Parent Groups sub folder:

Take the get-parent-group-recursively for example, all the parent groups should be returned:
{
    "id": "-13eae784:15ea2013504:-7ff7",
    "result": {
        "success": true,
        "status": 200,
        "metadata": null,
        "content": {
            "id": "4",
            "modifiedBy": "1",
            "modifiedAt": "2017-08-21T16:00:00.000Z",
            "createdBy": "1",
            "createdAt": "2017-08-21T16:00:00.000Z",
            "name": "group 4",
            "description": "group 4 desc",
            "privateGroup": false,
            "selfRegister": false,
            "subGroups": null,
            "parentGroup": {
                "id": "2",
                "modifiedBy": "1",
                "modifiedAt": "2017-08-21T16:00:00.000Z",
                "createdBy": "1",
                "createdAt": "2017-08-21T16:00:00.000Z",
                "name": "group 2",
                "description": "group 2 desc",
                "privateGroup": false,
                "selfRegister": false,
                "subGroups": null,
                "parentGroup": {
                    "id": "1",
                    "modifiedBy": "1",
                    "modifiedAt": "2017-08-21T16:00:00.000Z",
                    "createdBy": "1",
                    "createdAt": "2017-08-21T16:00:00.000Z",
                    "name": "group 1",
                    "description": "group 1 desc",
                    "privateGroup": false,
                    "selfRegister": false,
                    "subGroups": null,
                    "parentGroup": null
                }
            }
        }
    },
    "version": "v3"
}