const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// Keeps track of the length of the 'likes' child list in a separate property.
exports.countattendeechange = functions.database.ref('/events/{eventId}/attendees/{personId}').onWrite(event => {
  const collectionRef = event.data.ref.parent;
  const countRef = collectionRef.parent.child('attendeeCount');

  // Return the promise from countRef.transaction() so our function 
  // waits for this async event to complete before it exits.
  return countRef.transaction(current => {
    if (event.data.exists() && !event.data.previous.exists()) {
      return (current || 0) + 1;
    }
    else if (!event.data.exists() && event.data.previous.exists()) {
      return (current || 0) - 1;
    }
  }).then(() => {
    console.log('Counter updated.');
  });
});

// If the number of likes gets deleted, recount the number of likes
exports.recountAttendees = functions.database.ref('/events/{eventId}/attendeeCount').onWrite(event => {
  if (!event.data.exists()) {
    const counterRef = event.data.ref;
    const collectionRef = counterRef.parent.child('attendees');
    
    // Return the promise from counterRef.set() so our function 
    // waits for this async event to complete before it exits.
    return collectionRef.once('value')
        .then(messagesData => counterRef.set(messagesData.numChildren()));
  }
});