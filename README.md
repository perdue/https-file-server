# Build

Build the project with

    $ cd src/scripts
    $ cat README
    $ vim ssl.properties
    $ ./genKeys.sh
    $ cd ../../
    $ mvn install

# Run

Now you can run your webapp with:

    $ java -jar target/https-file-server-<version>.jar /path/to/base/dir

