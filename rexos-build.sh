#!/usr/bin/env sh
REXOS_BUILD_TARGET=""
ROS_ONLY=false
MAS_ONLY=false

function usage() {
	echo "Builds or cleans the cpp and java parts for the REXOS project."
	echo "Usage: source rexos-build.sh [-c] [-r] [-m]"
	echo "Defaults to build both ROS and MAS. Use -c to clean, -r to only build ROS, and -m to only build MAS."
}


#Have to clear OPTIND because this file as sourced and OPTIND is only cleared when creating a new shell.
OPTIND=0
while getopts ":chrm" opt; do
	case $opt in
		c)
			REXOS_BUILD_TARGET="clean"
			;;
		h)
			usage
			return
			;;
		r)
			ROS_ONLY=true
			;;
		m)
			MAS_ONLY=true
			;;
		\?)
			usage
			return
			;;
	esac
done

echo -e "\033[36m===== Setting ROS_PACKAGE_PATH =====\033[0m"
. ./.export-rospath
if [ "$MAS_ONLY" == false ] || [ "$ROS_ONLY" == true ];
then
	echo -e "\033[36m===== Building C++ =====\033[0m"
	catkin_make $REXOS_BUILD_TARGET
	echo -e "\033[35m===== DONE BUILDING C++ =====\033[0m"

	if [ "$REXOS_BUILD_TARGET" != "clean" ];
	then
		. ./devel/setup.sh
	fi
	
	#rosrun apparently caches its module list. Force an update so tab complete works.
	rospack list > /dev/null
fi

echo ""
if [ "$ROS_ONLY" == false ] || [ "$MAS_ONLY" == true ];
then
	echo -e "\033[36m===== Building JAVA =====\033[0m"
	ant $REXOS_BUILD_TARGET
	echo -e "\033[35m===== DONE JAVA =====\033[0m"
fi

#Have to clear OPTIND because this file as sourced and OPTIND is only cleared when creating a new shell.
OPTIND=0
unset REXOS_BUILD_TARGET
