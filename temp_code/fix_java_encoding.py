# -*- coding: utf-8 -*-
with open('src/main/java/com/example/demo/controller/UserController.java', 'r', encoding='utf-8-sig') as f:
    text = f.read()

# I also need to replace the buggy countDailyActivitiesByUserIdAndDateRange call.
# Let me check if the text still has it or if I broke the file.
text = text.replace('countDailyActivitiesByUserIdAndDateRange', 'countDailyAttempts')

with open('src/main/java/com/example/demo/controller/UserController.java', 'w', encoding='utf-8') as f:
    f.write(text)
