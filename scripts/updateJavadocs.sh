echo "User permissions"
ls -l

echo "Moving to working directory:"
cd ../Plan
pwd
echo "Building javadocs with gradle"
./gradlew aggregateJavadocs

echo "Moving to working directory:"
cd ../
pwd

echo "Copying files to temporary folder.."
mkdir temp
mkdir temp/all
mkdir temp/api
cp -r Plan/build/docs/javadoc/* temp/all/
cp -r Plan/api/build/docs/javadoc/* temp/api/

echo "Changing to gh-pages branch"
git checkout gh-pages

echo "Removing old javadocs"
rm -rf all
rm -rf api

echo "Copying files to /all and /api"
mkdir all
mkdir api
cp -r temp/all/* ./all
cp -r temp/api/* ./api
echo "Cleaning up.."
rm -rf temp

git add .
git commit -m "Updated javadocs"
git push

echo "Changing back to previous branch"
git checkout -

echo "Complete."
