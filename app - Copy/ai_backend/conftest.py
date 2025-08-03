import os
import pytest
import sys
from unittest.mock import MagicMock

# Add the app directory to Python path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../..'))


@pytest.fixture
def mock_api_key():
    """
    Return a static mock API key string for Genesis API integration tests.
    
    Returns:
        str: A fixed API key value used for testing.
    """
    return "test_api_key_12345"


@pytest.fixture
def mock_base_url():
    """
    Return a static mock base URL for the Genesis API endpoint used in integration tests.
    
    Returns:
        str: Mock Genesis API base URL.
    """
    return "https://api.genesis.test"


@pytest.fixture
def sample_api_response():
    """
    Return a mock dictionary representing a successful Genesis API chat completion response.
    
    The response includes typical fields such as response ID, object type, creation timestamp, model name, assistant message, finish reason, and token usage. Useful for tests that require a realistic Genesis API response structure.
    
    Returns:
        dict: Simulated Genesis API chat completion response.
    """
    return {
        "id": "test_response_id",
        "object": "chat.completion",
        "created": 1234567890,
        "model": "genesis-1",
        "choices": [
            {
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": "This is a test response from Genesis API"
                },
                "finish_reason": "stop"
            }
        ],
        "usage": {
            "prompt_tokens": 25,
            "completion_tokens": 50,
            "total_tokens": 75
        }
    }


@pytest.fixture
def sample_error_response():
    """
    Return a dictionary simulating an error response from the Genesis API.
    
    The response includes an `error` object with fields for error type, message, parameter, and code, suitable for testing error handling scenarios.
    
    Returns:
        dict: Mock Genesis API error response.
    """
    return {
        "error": {
            "type": "invalid_request_error",
            "message": "Invalid request parameters",
            "param": "model",
            "code": "invalid_model"
        }
    }


@pytest.fixture(autouse=True)
def mock_environment():
    """
    Automatically sets and cleans up Genesis API environment variables for each test.
    
    This autouse pytest fixture ensures that `GENESIS_API_KEY` and `GENESIS_BASE_URL` are set to test values before each test and removed afterward to maintain test isolation.
    """
    os.environ["GENESIS_API_KEY"] = "test_env_key"
    os.environ["GENESIS_BASE_URL"] = "https://api.genesis.test"
    yield
    # Cleanup
    if "GENESIS_API_KEY" in os.environ:
        del os.environ["GENESIS_API_KEY"]
    if "GENESIS_BASE_URL" in os.environ:
        del os.environ["GENESIS_BASE_URL"]
