export default [
  {
    'module': {
      'id': 21906,
      'name': 'AcademyApiWeb',
      'type': 'maven',
      'path': 'AcademyApiWeb/pom.xml',
      'glob': 'AcademyApiWeb/**',
      'active': true,
      'createdTimestamp': 1459948108000,
      'updatedTimestamp': 1471860876000,
      'buildpack': {
        'host': 'git.hubteam.com',
        'organization': 'HubSpot',
        'repository': 'Blazar-Buildpack-Java',
        'repositoryId': 0,
        'branch': 'v2-deployable',
        'active': false,
        'createdTimestamp': 1471467413444,
        'updatedTimestamp': 1471467413444
      }
    },
    'lastSuccessfulBuild': {
      'id': 1011314,
      'repoBuildId': 139317,
      'moduleId': 21906,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860845872,
      'endTimestamp': 1471860876642,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750659-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'upload',
            'description': 'Creating deployable slug',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /tmp/deploy-slug'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cd /tmp/deploy-slug && mkdir -p app bin conf logs'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp hubspot.build.json /tmp/deploy-slug/conf/build.json'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp -r target /tmp/deploy-slug/app/target'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'tar czf artifact.tgz -C /tmp/deploy-slug .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'upload',
            'description': 'Uploading deployable slug to S3',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'upload-artifact --auto-sign --artifact artifact.tgz'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'upload': {
            'branches': [
              'master'
            ]
          },
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastNonSkippedBuild': {
      'id': 1011314,
      'repoBuildId': 139317,
      'moduleId': 21906,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860845872,
      'endTimestamp': 1471860876642,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750659-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'upload',
            'description': 'Creating deployable slug',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /tmp/deploy-slug'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cd /tmp/deploy-slug && mkdir -p app bin conf logs'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp hubspot.build.json /tmp/deploy-slug/conf/build.json'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp -r target /tmp/deploy-slug/app/target'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'tar czf artifact.tgz -C /tmp/deploy-slug .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'upload',
            'description': 'Uploading deployable slug to S3',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'upload-artifact --auto-sign --artifact artifact.tgz'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'upload': {
            'branches': [
              'master'
            ]
          },
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastBuild': {
      'id': 1011314,
      'repoBuildId': 139317,
      'moduleId': 21906,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860845872,
      'endTimestamp': 1471860876642,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750659-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'upload',
            'description': 'Creating deployable slug',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /tmp/deploy-slug'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cd /tmp/deploy-slug && mkdir -p app bin conf logs'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp hubspot.build.json /tmp/deploy-slug/conf/build.json'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp -r target /tmp/deploy-slug/app/target'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'tar czf artifact.tgz -C /tmp/deploy-slug .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'upload',
            'description': 'Uploading deployable slug to S3',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'upload-artifact --auto-sign --artifact artifact.tgz'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'upload': {
            'branches': [
              'master'
            ]
          },
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'inProgressBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    },
    'pendingBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    }
  },
  {
    'module': {
      'id': 10734,
      'name': 'TestService',
      'type': 'maven',
      'path': 'TestService/pom.xml',
      'glob': 'TestService/**',
      'active': true,
      'createdTimestamp': 1458153868000,
      'updatedTimestamp': 1471970246000,
      'buildpack': {
        'host': 'git.hubteam.com',
        'organization': 'HubSpot',
        'repository': 'Blazar-Buildpack-Java',
        'repositoryId': 0,
        'branch': 'v2-deployable',
        'active': false,
        'createdTimestamp': 1469727508292,
        'updatedTimestamp': 1469727508292
      }
    },
    'lastSuccessfulBuild': {
      'id': 1023549,
      'repoBuildId': 141241,
      'moduleId': 10734,
      'buildNumber': 252,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471967877734,
      'endTimestamp': 1471967938424,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-190_165_165-1471967878035-8-cruel_night.iad03.hubspot_networks.net-us_east_1b',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'upload',
            'description': 'Creating deployable slug',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /tmp/deploy-slug'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cd /tmp/deploy-slug && mkdir -p app bin conf logs'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp hubspot.build.json /tmp/deploy-slug/conf/build.json'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp -r target /tmp/deploy-slug/app/target'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'tar czf artifact.tgz -C /tmp/deploy-slug .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'upload',
            'description': 'Uploading deployable slug to S3',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'upload-artifact --auto-sign --artifact artifact.tgz'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'upload': {
            'branches': [
              'master'
            ]
          },
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastNonSkippedBuild': {
      'id': 1023959,
      'repoBuildId': 141312,
      'moduleId': 10734,
      'buildNumber': 253,
      'state': 'IN_PROGRESS',
      'startTimestamp': 1471970246744,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-190_165_165-1471970247201-4-great_seal.iad03.hubspot_networks.net-us_east_1b',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'upload',
            'description': 'Creating deployable slug',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /tmp/deploy-slug'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cd /tmp/deploy-slug && mkdir -p app bin conf logs'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp hubspot.build.json /tmp/deploy-slug/conf/build.json'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp -r target /tmp/deploy-slug/app/target'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'tar czf artifact.tgz -C /tmp/deploy-slug .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'upload',
            'description': 'Uploading deployable slug to S3',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'upload-artifact --auto-sign --artifact artifact.tgz'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'upload': {
            'branches': [
              'master'
            ]
          },
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastBuild': {
      'id': 1023549,
      'repoBuildId': 141241,
      'moduleId': 10734,
      'buildNumber': 252,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471967877734,
      'endTimestamp': 1471967938424,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-190_165_165-1471967878035-8-cruel_night.iad03.hubspot_networks.net-us_east_1b',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'upload',
            'description': 'Creating deployable slug',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /tmp/deploy-slug'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cd /tmp/deploy-slug && mkdir -p app bin conf logs'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp hubspot.build.json /tmp/deploy-slug/conf/build.json'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp -r target /tmp/deploy-slug/app/target'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'tar czf artifact.tgz -C /tmp/deploy-slug .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'upload',
            'description': 'Uploading deployable slug to S3',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'upload-artifact --auto-sign --artifact artifact.tgz'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'upload': {
            'branches': [
              'master'
            ]
          },
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'inProgressBuild': {
      'id': 1023959,
      'repoBuildId': 141312,
      'moduleId': 10734,
      'buildNumber': 253,
      'state': 'IN_PROGRESS',
      'startTimestamp': 1471970246744,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-190_165_165-1471970247201-4-great_seal.iad03.hubspot_networks.net-us_east_1b',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'upload',
            'description': 'Creating deployable slug',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /tmp/deploy-slug'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cd /tmp/deploy-slug && mkdir -p app bin conf logs'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp hubspot.build.json /tmp/deploy-slug/conf/build.json'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp -r target /tmp/deploy-slug/app/target'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'tar czf artifact.tgz -C /tmp/deploy-slug .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'upload',
            'description': 'Uploading deployable slug to S3',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'upload-artifact --auto-sign --artifact artifact.tgz'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'upload': {
            'branches': [
              'master'
            ]
          },
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'pendingBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    }
  },
  {
    'module': {
      'id': 1436,
      'name': 'BidenClient',
      'type': 'maven',
      'path': 'BidenClient/pom.xml',
      'glob': 'BidenClient/**',
      'active': true,
      'createdTimestamp': 1455727604000,
      'updatedTimestamp': 1472222523000,
      'buildpack': {
        'host': 'git.hubteam.com',
        'organization': 'HubSpot',
        'repository': 'Blazar-Buildpack-Java',
        'repositoryId': 0,
        'branch': 'v2',
        'active': false,
        'createdTimestamp': 1471467413444,
        'updatedTimestamp': 1471467413444
      }
    },
    'lastSuccessfulBuild': {
      'id': 992175,
      'repoBuildId': 136226,
      'moduleId': 1436,
      'buildNumber': 159,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471549852048,
      'endTimestamp': 1471549919529,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471549853268-2-sore_coke.iad03.hubspot_networks.net-us_east_1e',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastNonSkippedBuild': {
      'id': 1065551,
      'repoBuildId': 147337,
      'moduleId': 1436,
      'buildNumber': 165,
      'state': 'FAILED',
      'startTimestamp': 1472222461417,
      'endTimestamp': 1472222523621,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-190_165_165-1472222462634-36-tall_truth.iad03.hubspot_networks.net-us_east_1a',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastBuild': {
      'id': 1065551,
      'repoBuildId': 147337,
      'moduleId': 1436,
      'buildNumber': 165,
      'state': 'FAILED',
      'startTimestamp': 1472222461417,
      'endTimestamp': 1472222523621,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-190_165_165-1472222462634-36-tall_truth.iad03.hubspot_networks.net-us_east_1a',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'inProgressBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    },
    'pendingBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    }
  },
  {
    'module': {
      'id': 21909,
      'name': 'Academy',
      'type': 'maven',
      'path': 'pom.xml',
      'glob': 'pom.xml',
      'active': true,
      'createdTimestamp': 1459948108000,
      'updatedTimestamp': 1471846599000,
      'buildpack': {
        'host': 'git.hubteam.com',
        'organization': 'HubSpot',
        'repository': 'Blazar-Buildpack-Java',
        'repositoryId': 0,
        'branch': 'v2',
        'active': false,
        'createdTimestamp': 1471467413444,
        'updatedTimestamp': 1471467413444
      }
    },
    'lastSuccessfulBuild': {
      'id': 1010735,
      'repoBuildId': 139221,
      'moduleId': 21909,
      'buildNumber': 175,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471846563300,
      'endTimestamp': 1471846599127,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471846564338-9-early_lake.iad03.hubspot_networks.net-us_east_1e',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastNonSkippedBuild': {
      'id': 1010735,
      'repoBuildId': 139221,
      'moduleId': 21909,
      'buildNumber': 175,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471846563300,
      'endTimestamp': 1471846599127,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471846564338-9-early_lake.iad03.hubspot_networks.net-us_east_1e',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastBuild': {
      'id': 1010735,
      'repoBuildId': 139221,
      'moduleId': 21909,
      'buildNumber': 175,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471846563300,
      'endTimestamp': 1471846599127,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471846564338-9-early_lake.iad03.hubspot_networks.net-us_east_1e',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'inProgressBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    },
    'pendingBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    }
  },
  {
    'module': {
      'id': 1442,
      'name': 'BidenKafka',
      'type': 'maven',
      'path': 'BidenKafka/pom.xml',
      'glob': 'BidenKafka/**',
      'active': true,
      'createdTimestamp': 1455727604000,
      'updatedTimestamp': 1472596119000,
      'buildpack': {
        'host': 'git.hubteam.com',
        'organization': 'HubSpot',
        'repository': 'Blazar-Buildpack-Java',
        'repositoryId': 0,
        'branch': 'v2-deployable',
        'active': false,
        'createdTimestamp': 1471645308460,
        'updatedTimestamp': 1471645308460
      }
    },
    'lastSuccessfulBuild': {
      'id': 1001272,
      'repoBuildId': 137301,
      'moduleId': 1442,
      'buildNumber': 161,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471621302974,
      'endTimestamp': 1471621343491,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471621181650-9-frail_gecko.iad03.hubspot_networks.net-us_east_1a',
      'buildConfig': {
        'steps': [],
        'before': [],
        'env': {},
        'buildDeps': [],
        'webhooks': [],
        'cache': [],
        'stepActivation': {},
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Linking local maven repository',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /usr/share/hubspot/mesos/blazar_cache/maven'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'ln -s /usr/share/hubspot/mesos/blazar_cache/maven ~/.m2/repository'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Overriding maven versions if on a branch',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  '[ "$GIT_BRANCH" == "master" ] || [ -n "$SKIP_VERSION_OVERRIDE" ] || set-maven-versions --version $SET_VERSION --root $WORKSPACE'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'SET_VERSION': '${SET_VERSION_OVERRIDE:-1.0-$GIT_BRANCH-SNAPSHOT}'
                }
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Creating hubspot.build.json metadata',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'create-build-json --workspace_path . --project_name $BLAZAR_COORDINATES'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'copy-build-json --search-dir .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running maven',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mvn $MAVEN_ARGS -DaltSnapshotDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/snapshots -DaltReleaseDeploymentRepository=s3::default::s3://hubspot-maven-artifacts-prod/releases -DaltDeploymentRepository=s3::default::s3://hubspot-upgrade-your-deploy-plugin -Dmaven.install.skip=true -DargLine=-Xmx$MAVEN_TEST_HEAP -B -N deploy'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Expiring Nexus cache',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'expire-nexus-cache --root .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'upload',
            'description': 'Creating deployable slug',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'mkdir -p /tmp/deploy-slug'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cd /tmp/deploy-slug && mkdir -p app bin conf logs'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp hubspot.build.json /tmp/deploy-slug/conf/build.json'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'cp -r target /tmp/deploy-slug/app/target'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'tar czf artifact.tgz -C /tmp/deploy-slug .'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'upload',
            'description': 'Uploading deployable slug to S3',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'upload-artifact --auto-sign --artifact artifact.tgz'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'notify',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'publish-build-success --project $BLAZAR_COORDINATES --build-num $MODULE_BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {
          'MAVEN_OPTS': '-Xmx512m',
          'HUBSPOT_ZK_ENVIRONMENT': 'local_local',
          'MAVEN_TEST_HEAP': '512m'
        },
        'buildDeps': [
          'hs-build-tools',
          'jdk8',
          'maven3'
        ],
        'webhooks': [],
        'cache': [],
        'user': 'hs-build',
        'stepActivation': {
          'upload': {
            'branches': [
              'master'
            ]
          },
          'notify': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastNonSkippedBuild': {
      'id': 1096978,
      'repoBuildId': 151773,
      'moduleId': 1442,
      'buildNumber': 169,
      'state': 'CANCELLED',
      'startTimestamp': 1472596119784,
      'endTimestamp': 1472596119787
    },
    'lastBuild': {
      'id': 1096978,
      'repoBuildId': 151773,
      'moduleId': 1442,
      'buildNumber': 169,
      'state': 'CANCELLED',
      'startTimestamp': 1472596119784,
      'endTimestamp': 1472596119787
    },
    'inProgressBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    },
    'pendingBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    }
  },
  {
    'module': {
      'id': 17149,
      'name': 'DeployerUI',
      'type': 'config',
      'path': '.blazar.yaml',
      'glob': '**',
      'active': false,
      'createdTimestamp': 1459280405000,
      'updatedTimestamp': 1462806335000
    },
    'lastSuccessfulBuild': {
      'id': 207330,
      'repoBuildId': 27567,
      'moduleId': 17149,
      'buildNumber': 76,
      'state': 'SUCCEEDED',
      'startTimestamp': 1462375987970,
      'endTimestamp': 1462376048533,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-118_106_106-1462375988244-4-raspy_forest.iad03.hubspot_networks.net-us_east_1a',
      'buildConfig': {
        'steps': [
          {
            'description': 'Installing dependencies',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'npm install'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'if [ -d ./node_modules/bender-deps ]; then\n  npm update bender-deps\nelse\n  npm install bender-deps\nfi\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Linting',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/gulp lint'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running gulp build',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'DEPLOYER_STATIC_ROOT=https://static.hsappstatic.net/DeployerUI/static-1.$BUILD_NUMBER ./node_modules/.bin/gulp build\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'publish',
            'description': 'Publishing to the CDN',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/bender-deps cdn-publish DeployerUI@1.$BUILD_NUMBER dist'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'publish',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'CI_ENVIRONMENT=PROD publish-build-success --project DeployerUI --version static-1.$BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {},
        'buildDeps': [
          'node-0.12.7',
          'hs-build-tools'
        ],
        'webhooks': [],
        'cache': [
          '$PWD/node_modules',
          '$PWD/bower_components'
        ],
        'stepActivation': {
          'publish': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Installing dependencies',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'npm install'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'if [ -d ./node_modules/bender-deps ]; then\n  npm update bender-deps\nelse\n  npm install bender-deps\nfi\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Linting',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/gulp lint'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running gulp build',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'DEPLOYER_STATIC_ROOT=https://static.hsappstatic.net/DeployerUI/static-1.$BUILD_NUMBER ./node_modules/.bin/gulp build\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'publish',
            'description': 'Publishing to the CDN',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/bender-deps cdn-publish DeployerUI@1.$BUILD_NUMBER dist'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'publish',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'CI_ENVIRONMENT=PROD publish-build-success --project DeployerUI --version static-1.$BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {},
        'buildDeps': [
          'node-0.12.7',
          'hs-build-tools'
        ],
        'webhooks': [],
        'cache': [
          '$PWD/node_modules',
          '$PWD/bower_components'
        ],
        'user': 'hs-build',
        'stepActivation': {
          'publish': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastNonSkippedBuild': {
      'id': 207330,
      'repoBuildId': 27567,
      'moduleId': 17149,
      'buildNumber': 76,
      'state': 'SUCCEEDED',
      'startTimestamp': 1462375987970,
      'endTimestamp': 1462376048533,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-118_106_106-1462375988244-4-raspy_forest.iad03.hubspot_networks.net-us_east_1a',
      'buildConfig': {
        'steps': [
          {
            'description': 'Installing dependencies',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'npm install'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'if [ -d ./node_modules/bender-deps ]; then\n  npm update bender-deps\nelse\n  npm install bender-deps\nfi\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Linting',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/gulp lint'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running gulp build',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'DEPLOYER_STATIC_ROOT=https://static.hsappstatic.net/DeployerUI/static-1.$BUILD_NUMBER ./node_modules/.bin/gulp build\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'publish',
            'description': 'Publishing to the CDN',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/bender-deps cdn-publish DeployerUI@1.$BUILD_NUMBER dist'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'publish',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'CI_ENVIRONMENT=PROD publish-build-success --project DeployerUI --version static-1.$BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {},
        'buildDeps': [
          'node-0.12.7',
          'hs-build-tools'
        ],
        'webhooks': [],
        'cache': [
          '$PWD/node_modules',
          '$PWD/bower_components'
        ],
        'stepActivation': {
          'publish': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Installing dependencies',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'npm install'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'if [ -d ./node_modules/bender-deps ]; then\n  npm update bender-deps\nelse\n  npm install bender-deps\nfi\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Linting',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/gulp lint'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running gulp build',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'DEPLOYER_STATIC_ROOT=https://static.hsappstatic.net/DeployerUI/static-1.$BUILD_NUMBER ./node_modules/.bin/gulp build\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'publish',
            'description': 'Publishing to the CDN',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/bender-deps cdn-publish DeployerUI@1.$BUILD_NUMBER dist'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'publish',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'CI_ENVIRONMENT=PROD publish-build-success --project DeployerUI --version static-1.$BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {},
        'buildDeps': [
          'node-0.12.7',
          'hs-build-tools'
        ],
        'webhooks': [],
        'cache': [
          '$PWD/node_modules',
          '$PWD/bower_components'
        ],
        'user': 'hs-build',
        'stepActivation': {
          'publish': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'lastBuild': {
      'id': 207330,
      'repoBuildId': 27567,
      'moduleId': 17149,
      'buildNumber': 76,
      'state': 'SUCCEEDED',
      'startTimestamp': 1462375987970,
      'endTimestamp': 1462376048533,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-118_106_106-1462375988244-4-raspy_forest.iad03.hubspot_networks.net-us_east_1a',
      'buildConfig': {
        'steps': [
          {
            'description': 'Installing dependencies',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'npm install'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'if [ -d ./node_modules/bender-deps ]; then\n  npm update bender-deps\nelse\n  npm install bender-deps\nfi\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Linting',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/gulp lint'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running gulp build',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'DEPLOYER_STATIC_ROOT=https://static.hsappstatic.net/DeployerUI/static-1.$BUILD_NUMBER ./node_modules/.bin/gulp build\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'publish',
            'description': 'Publishing to the CDN',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/bender-deps cdn-publish DeployerUI@1.$BUILD_NUMBER dist'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'publish',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'CI_ENVIRONMENT=PROD publish-build-success --project DeployerUI --version static-1.$BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {},
        'buildDeps': [
          'node-0.12.7',
          'hs-build-tools'
        ],
        'webhooks': [],
        'cache': [
          '$PWD/node_modules',
          '$PWD/bower_components'
        ],
        'stepActivation': {
          'publish': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      },
      'resolvedConfig': {
        'steps': [
          {
            'description': 'Installing dependencies',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'npm install'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              },
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'if [ -d ./node_modules/bender-deps ]; then\n  npm update bender-deps\nelse\n  npm install bender-deps\nfi\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Linting',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/gulp lint'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'description': 'Running gulp build',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'DEPLOYER_STATIC_ROOT=https://static.hsappstatic.net/DeployerUI/static-1.$BUILD_NUMBER ./node_modules/.bin/gulp build\n'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': true
          },
          {
            'name': 'publish',
            'description': 'Publishing to the CDN',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  './node_modules/.bin/bender-deps cdn-publish DeployerUI@1.$BUILD_NUMBER dist'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {}
              }
            ],
            'activeByDefault': false
          },
          {
            'name': 'publish',
            'description': 'Notifying deploy service',
            'commands': [
              {
                'executable': '/bin/bash',
                'args': [
                  '-c',
                  'CI_ENVIRONMENT=PROD publish-build-success --project DeployerUI --version static-1.$BUILD_NUMBER'
                ],
                'successfulReturnCodes': [
                  0
                ],
                'env': {
                  'BOOTSTRAP_PROPERTIES_PATH': '/usr/share/hubspot/internal/hubspot.bootstrap.properties'
                }
              }
            ],
            'activeByDefault': false
          }
        ],
        'before': [],
        'env': {},
        'buildDeps': [
          'node-0.12.7',
          'hs-build-tools'
        ],
        'webhooks': [],
        'cache': [
          '$PWD/node_modules',
          '$PWD/bower_components'
        ],
        'user': 'hs-build',
        'stepActivation': {
          'publish': {
            'branches': [
              'master'
            ]
          }
        },
        'depends': [],
        'provides': []
      }
    },
    'inProgressBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    },
    'pendingBuild': {
      'repoBuildId': 0,
      'moduleId': 0,
      'buildNumber': 0
    }
  }
];
