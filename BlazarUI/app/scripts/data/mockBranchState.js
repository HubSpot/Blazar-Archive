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
      'id': 21908,
      'name': 'AcademyCore',
      'type': 'maven',
      'path': 'AcademyCore/pom.xml',
      'glob': 'AcademyCore/**',
      'active': true,
      'createdTimestamp': 1459948108000,
      'updatedTimestamp': 1471860781000,
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
      'id': 1011312,
      'repoBuildId': 139317,
      'moduleId': 21908,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860749749,
      'endTimestamp': 1471860781549,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750621-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 1011312,
      'repoBuildId': 139317,
      'moduleId': 21908,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860749749,
      'endTimestamp': 1471860781549,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750621-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 1011312,
      'repoBuildId': 139317,
      'moduleId': 21908,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860749749,
      'endTimestamp': 1471860781549,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750621-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 21905,
      'name': 'AcademyJobs',
      'type': 'maven',
      'path': 'AcademyJobs/pom.xml',
      'glob': 'AcademyJobs/**',
      'active': true,
      'createdTimestamp': 1459948108000,
      'updatedTimestamp': 1471860877000,
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
      'id': 1011313,
      'repoBuildId': 139317,
      'moduleId': 21905,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860845739,
      'endTimestamp': 1471860877992,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750640-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 1011313,
      'repoBuildId': 139317,
      'moduleId': 21905,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860845739,
      'endTimestamp': 1471860877992,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750640-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 1011313,
      'repoBuildId': 139317,
      'moduleId': 21905,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860845739,
      'endTimestamp': 1471860877992,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750640-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 21907,
      'name': 'AcademyClient',
      'type': 'maven',
      'path': 'AcademyClient/pom.xml',
      'glob': 'AcademyClient/**',
      'active': true,
      'createdTimestamp': 1459948108000,
      'updatedTimestamp': 1471860806000,
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
      'id': 1011316,
      'repoBuildId': 139317,
      'moduleId': 21907,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860782892,
      'endTimestamp': 1471860806488,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750603-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 1011316,
      'repoBuildId': 139317,
      'moduleId': 21907,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860782892,
      'endTimestamp': 1471860806488,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750603-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 1011316,
      'repoBuildId': 139317,
      'moduleId': 21907,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860782892,
      'endTimestamp': 1471860806488,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750603-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 21904,
      'name': 'AcademyData',
      'type': 'maven',
      'path': 'AcademyData/pom.xml',
      'glob': 'AcademyData/**',
      'active': true,
      'createdTimestamp': 1459948108000,
      'updatedTimestamp': 1471860844000,
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
      'id': 1011315,
      'repoBuildId': 139317,
      'moduleId': 21904,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860782751,
      'endTimestamp': 1471860844190,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750559-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 1011315,
      'repoBuildId': 139317,
      'moduleId': 21904,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860782751,
      'endTimestamp': 1471860844190,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750559-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
      'id': 1011315,
      'repoBuildId': 139317,
      'moduleId': 21904,
      'buildNumber': 176,
      'state': 'SUCCEEDED',
      'startTimestamp': 1471860782751,
      'endTimestamp': 1471860844190,
      'taskId': 'BlazarExecutorJava-blazar-executor-java-189_164_164-1471860750559-1-sore_coke.iad03.hubspot_networks.net-us_east_1e',
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
  }
];
