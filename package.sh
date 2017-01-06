#!/bin/sh

###################
# Package Nxt2API #
###################

if [ -z "$1" ] ; then
  echo "You must specify the version to package"
  exit 1
fi

if [ ! -d target/site/apidocs ] ; then
  echo "Creating the Nxt2API documentation"
  mvn javadoc:javadoc
fi

VERSION="$1"

if [ ! -d package ] ; then
  mkdir package
fi

cd package
rm -R *
mkdir apidocs 
cp ../ChangeLog.txt ../LICENSE ../README.md .
cp ../target/*.jar .
cp -r ../target/site/apidocs/* apidocs
zip -r Nxt2API-$VERSION.zip ChangeLog.txt LICENSE README.md *.jar apidocs
dos2unix ChangeLog.txt LICENSE README.md 
tar zcf Nxt2API-$VERSION.tar.gz ChangeLog.txt LICENSE README.md *.jar apidocs
exit 0

