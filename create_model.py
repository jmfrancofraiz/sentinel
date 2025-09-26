#!/usr/bin/env python3
"""
Create a TensorFlow Lite model for WhatsApp contact name extraction.
This model takes text input and outputs contact names.
"""

import tensorflow as tf
import numpy as np
import os

def create_contact_detection_model():
    """
    Create a simple neural network model for contact name extraction.
    Input: Text sequences (max 200 characters)
    Output: Contact names (max 50 characters)
    """
    
    # Model parameters
    MAX_INPUT_LENGTH = 200
    MAX_OUTPUT_LENGTH = 50
    VOCAB_SIZE = 30000  # Character vocabulary size
    
    # Input layer
    input_text = tf.keras.Input(shape=(MAX_INPUT_LENGTH,), name='input_text', dtype=tf.float32)
    
    # Simple dense layers for TFLite compatibility
    dense1 = tf.keras.layers.Dense(256, activation='relu', name='dense1')(input_text)
    dropout1 = tf.keras.layers.Dropout(0.2, name='dropout1')(dense1)
    
    dense2 = tf.keras.layers.Dense(128, activation='relu', name='dense2')(dropout1)
    dropout2 = tf.keras.layers.Dropout(0.2, name='dropout2')(dense2)
    
    dense3 = tf.keras.layers.Dense(64, activation='relu', name='dense3')(dropout2)
    dropout3 = tf.keras.layers.Dropout(0.1, name='dropout3')(dense3)
    
    # Output layer - character-level prediction
    output = tf.keras.layers.Dense(
        MAX_OUTPUT_LENGTH * VOCAB_SIZE,
        activation='softmax',
        name='output'
    )(dropout3)
    
    # Reshape output to (batch_size, MAX_OUTPUT_LENGTH, VOCAB_SIZE)
    output_reshaped = tf.keras.layers.Reshape(
        (MAX_OUTPUT_LENGTH, VOCAB_SIZE),
        name='output_reshaped'
    )(output)
    
    # Create model
    model = tf.keras.Model(inputs=input_text, outputs=output_reshaped, name='contact_detection_model')
    
    return model

def generate_training_data():
    """
    Generate synthetic training data for the model.
    In a real scenario, this would be actual WhatsApp UI text data.
    """
    # Sample training data (text -> contact_name)
    training_examples = [
        ("John Smith\nOnline\nLast seen today at 2:30 PM", "John Smith"),
        ("Maria Garcia\nTyping...\nLast seen yesterday", "Maria Garcia"),
        ("Dr. Johnson\nOnline\nLast seen 5 minutes ago", "Dr. Johnson"),
        ("Sarah Wilson\nLast seen today at 10:15 AM", "Sarah Wilson"),
        ("Mike Chen\nOnline\nLast seen 1 hour ago", "Mike Chen"),
        ("Anna Rodriguez\nLast seen yesterday at 8:45 PM", "Anna Rodriguez"),
        ("Prof. Brown\nOnline\nLast seen 30 minutes ago", "Prof. Brown"),
        ("Lisa Johnson\nTyping...\nLast seen today", "Lisa Johnson"),
        ("David Kim\nLast seen 2 hours ago", "David Kim"),
        ("Emma Davis\nOnline\nLast seen 15 minutes ago", "Emma Davis"),
    ]
    
    # Generate more examples
    names = ["Alex", "Chris", "Taylor", "Jordan", "Casey", "Morgan", "Riley", "Avery", "Quinn", "Blake"]
    surnames = ["Anderson", "Taylor", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez"]
    
    for i in range(50):
        first_name = np.random.choice(names)
        last_name = np.random.choice(surnames)
        full_name = f"{first_name} {last_name}"
        
        status_options = ["Online", "Last seen today", "Last seen yesterday", "Typing..."]
        status = np.random.choice(status_options)
        
        text = f"{full_name}\n{status}\nLast seen today at {np.random.randint(1, 12)}:{np.random.randint(10, 60):02d} PM"
        training_examples.append((text, full_name))
    
    return training_examples

def preprocess_text(text, max_length=200):
    """Convert text to character indices."""
    # Simple character encoding (ASCII + some Unicode)
    char_indices = []
    for char in text[:max_length]:
        char_code = min(ord(char), 29999)  # Cap at VOCAB_SIZE - 1
        char_indices.append(char_code)
    
    # Pad to max_length
    while len(char_indices) < max_length:
        char_indices.append(0)  # Padding token
    
    return char_indices

def preprocess_contact_name(name, max_length=50):
    """Convert contact name to character indices."""
    char_indices = []
    for char in name[:max_length]:
        char_code = min(ord(char), 29999)
        char_indices.append(char_code)
    
    # Pad to max_length
    while len(char_indices) < max_length:
        char_indices.append(0)
    
    return char_indices

def create_training_data():
    """Create training data for the model."""
    training_examples = generate_training_data()
    
    X = []
    y = []
    
    for text, contact_name in training_examples:
        # Preprocess input text
        text_indices = preprocess_text(text)
        X.append(text_indices)
        
        # Preprocess output contact name
        name_indices = preprocess_contact_name(contact_name)
        y.append(name_indices)
    
    return np.array(X, dtype=np.float32), np.array(y, dtype=np.float32)

def train_model():
    """Train the contact detection model."""
    print("Creating model...")
    model = create_contact_detection_model()
    
    print("Model architecture:")
    model.summary()
    
    print("Generating training data...")
    X_train, y_train = create_training_data()
    
    print(f"Training data shape: X={X_train.shape}, y={y_train.shape}")
    
    # Compile model
    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )
    
    # Train model
    print("Training model...")
    history = model.fit(
        X_train, y_train,
        epochs=10,
        batch_size=8,
        validation_split=0.2,
        verbose=1
    )
    
    return model

def convert_to_tflite(model, output_path):
    """Convert the trained model to TensorFlow Lite format."""
    print(f"Converting model to TensorFlow Lite format...")
    
    # Convert to TensorFlow Lite
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Basic optimization settings for compatibility
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    # Convert
    tflite_model = converter.convert()
    
    # Save model
    with open(output_path, 'wb') as f:
        f.write(tflite_model)
    
    print(f"Model saved to: {output_path}")
    print(f"Model size: {len(tflite_model) / 1024 / 1024:.2f} MB")

def main():
    """Main function to create and train the model."""
    print("WhatsApp Contact Detection Model Creator")
    print("=" * 50)
    
    # Create output directory
    output_dir = "app/src/main/assets/models"
    os.makedirs(output_dir, exist_ok=True)
    
    # Train model
    model = train_model()
    
    # Convert to TensorFlow Lite
    tflite_path = os.path.join(output_dir, "contact_detection_model.tflite")
    convert_to_tflite(model, tflite_path)
    
    print("\nModel creation completed successfully!")
    print(f"TensorFlow Lite model saved to: {tflite_path}")

if __name__ == "__main__":
    main()
