insert into JavaSoftware
(buildNumber, jarFile, className)
values(1, "", "equiplet");

insert into RosSoftware
(buildNumber, zipFile, command)
values(1, "", "rosrun equiplet_node equiplet_node {equipletName}");

insert into Equiplet
(name, mountPointsX, mountPointsY, mountPointDistanceX, mountPointDistanceY, rosSoftware, masSoftware)
values("EQ1", 10, 10, 50, 50, 1, 1);