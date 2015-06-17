# coding: utf-8

import datetime
import random

d = datetime.datetime.now() - datetime.timedelta(days=90)
o = 17

for i in xrange(24 * 90):
  sec = 3600 + random.randint(-600, 600)
  d += datetime.timedelta(seconds=sec)
  timestamp = str(d).split('.')[0]

  print 'INSERT INTO TestExecution VALUES (DEFAULT, NULL, TIMESTAMP(\'%s\'));' % timestamp

  for j in xrange(5):
    result = str(random.random() < 0.6).upper()
    env = random.randint(1, 3)
    print 'INSERT INTO Screenshot VALUES (DEFAULT, 1, \'sample\', %s, \'SampleClass\', \'SampleMethod\', \'SampleScreen%d\', %d, %d);' % (result, j, o + i, env)

print 'COMMIT;'
