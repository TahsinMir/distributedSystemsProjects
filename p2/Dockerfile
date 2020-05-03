FROM centos:7
# Maintainer of this docker file
LABEL maintainer="golammdmortuza@u.boisestate.edu"

# Install required program
RUN yum -y install java-1.8.0-openjdk-devel net-tools make
RUN yum -y install maven

# Copyting the file to docker container
RUN (mkdir /p2)
COPY . /p2

# Move in the correct directory

#WORKDIR /p2/

#RUN (make)

ENTRYPOINT ["/bin/bash"]
# Install the ls program

